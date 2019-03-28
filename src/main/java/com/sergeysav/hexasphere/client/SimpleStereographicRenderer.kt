package com.sergeysav.hexasphere.client

import com.sergeysav.hexasphere.client.camera.Camera
import com.sergeysav.hexasphere.client.camera.CameraController
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.client.gl.bound
import com.sergeysav.hexasphere.client.world.WorldRenderable
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.loadResource
import com.sergeysav.hexasphere.common.setUniform
import com.sergeysav.hexasphere.common.world.getClosestTileTo
import com.sergeysav.hexasphere.common.world.tile.Tile
import org.lwjgl.opengl.GL20

/**
 * @author sergeys
 *
 * @constructor Creates a new NormalRenderer
 */
class SimpleStereographicRenderer(val linAlgPool: LinAlgPool):
        Renderer {
    
    val shaderProgram = ShaderProgram()
//    private val mat4 = Matrix4f()
//    private val mat3 = Matrix3f()
//    private val tempV2 = Vector2f()
//    private val tempV3 = Vector3f()
//    private val temp2V3 = Vector3f()
//    private val tempV4 = Vector4f()
    
    init {
        shaderProgram.createVertexShader(
                loadResource("/stereographic_simple.vertex.glsl"))
        shaderProgram.createFragmentShader(loadResource("/fragment.glsl"))
        shaderProgram.link()
    
        GL20.glUniform1i(shaderProgram.getUniform("texture1"), 0)
    }
    
    override fun render(worldRenderable: WorldRenderable, camera: Camera) {
        worldRenderable.texture.bound {
            shaderProgram.bound {
                val scaling = camera.position.length() - 1.175f
                linAlgPool.mat3 { mat3 ->
                    mat3.scaling(1 / scaling, 1 / scaling * camera.aspect, 1f)
                    mat3.setUniform(shaderProgram.getUniform("uCamera"))
                    linAlgPool.mat4 { mat4 ->
                        mat4.set(mat3.set(camera.right, camera.up, camera.direction).transpose()).mul(worldRenderable.modelMatrix)
                        mat4.setUniform(shaderProgram.getUniform("uModel"))
                    }
                }
                worldRenderable.mesh.draw()
            }
        }
    }
    
    override fun getMouseoverTile(x: Float, y: Float, map: WorldRenderable,
                                  cameraController: CameraController): Tile? {
        val scaling = cameraController.camera.position.length() - 1.175f
        
        return linAlgPool.vec3 { tempV3 ->
            // -y needed to work
            tempV3.set(x, -y, 1f) // Screen Coordinates (result)
            linAlgPool.mat3 {scalingMatrix ->
                scalingMatrix.scaling(1 / scaling, 1 / scaling * cameraController.camera.aspect, 1f) // Camera Scaling matrix
                tempV3.mul(scalingMatrix.invert()) // projection result
            }
            val r2 = tempV3.x * tempV3.x + tempV3.y * tempV3.y // circle radius
            tempV3.set(2 * tempV3.x / (1 + r2), 2 * tempV3.y / (1 + r2), (-1 + r2)/(1 + r2)) //Base Sphere location
            linAlgPool.vec4 { tempV4 ->
                linAlgPool.mat4 { rotationMatrix ->
                    linAlgPool.mat3 { mat3 ->
                        rotationMatrix.set(mat3.set(cameraController.camera.right, cameraController.camera.up,
                                          cameraController.camera.direction).transpose())
                                .mul(map.modelMatrix) // Model rotation matrix
                    }
                    tempV4.set(tempV3.x, tempV3.y, tempV3.z, 1.0f).mul(rotationMatrix.invert()) // Rotate onto model
                }
                tempV3.set(tempV4.x / tempV4.w, tempV4.y / tempV4.w, tempV4.z / tempV4.w)
            }
            map.world.getClosestTileTo(tempV3, linAlgPool)
        }
    }
    
    override fun cleanup() {
        shaderProgram.cleanup()
    }
}