package com.sergeysav.hexasphere.client

import com.sergeysav.hexasphere.client.camera.Camera
import com.sergeysav.hexasphere.client.camera.CameraController
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.client.world.WorldRenderable
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.loadResource
import com.sergeysav.hexasphere.common.setUniform
import com.sergeysav.hexasphere.common.world.getClosestTileTo
import com.sergeysav.hexasphere.common.world.tile.Tile
import org.joml.Vector3f
import org.lwjgl.opengl.GL20

/**
 * @author sergeys
 *
 * @constructor Creates a new SimpleStereographicRenderer
 */
class SimpleStereographicRenderer(val linAlgPool: LinAlgPool): Renderer {
    
    val shader = ShaderProgram()
    private val lightDirection = Vector3f(1f, 1f, 1f).normalize()
    private val cameraOffset = 1.072f
    
    init {
        shader.createVertexShader(loadResource("/shader/stereographic_model.vertex.glsl"))
        shader.createFragmentShader(loadResource("/shader/stereographic_model.fragment.glsl"))
        shader.link()
    }
    
    override fun render(worldRenderable: WorldRenderable, camera: Camera) {
        shader.bound {
            val scaling = camera.position.length() - cameraOffset
            linAlgPool.mat3 { mat3 ->
                mat3.scaling(1 / scaling, 1 / scaling * camera.aspect, 1f)
                mat3.setUniform(shader.getUniform("uCamera"))
                linAlgPool.mat4 { mat4 ->
                    mat4.set(mat3.set(camera.right, camera.up, camera.direction).transpose()).mul(worldRenderable.modelMatrix)
                    mat4.setUniform(shader.getUniform("uModel"))
                }
            }
            GL20.glUniform3f(shader.getUniform("viewPos"), camera.position.x(), camera.position.y(), camera.position.z())
            GL20.glUniform3f(shader.getUniform("lightDir"), lightDirection.x(), lightDirection.y(), lightDirection.z())
            GL20.glUniform1f(shader.getUniform("ambientStrength"), 0.0f)
            worldRenderable.draw(shader)
        }
    }
    
    override fun getMouseoverTile(x: Float, y: Float, map: WorldRenderable,
                                  cameraController: CameraController): Tile? {
        val scaling = cameraController.camera.position.length() - cameraOffset
        
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
        shader.cleanup()
    }
}