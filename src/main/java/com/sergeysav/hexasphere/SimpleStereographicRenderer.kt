package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.ShaderProgram
import com.sergeysav.hexasphere.gl.bound
import com.sergeysav.hexasphere.map.Map
import com.sergeysav.hexasphere.map.tile.FinishedTile
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * @author sergeys
 *
 * @constructor Creates a new NormalRenderer
 */
class SimpleStereographicRenderer : Renderer {
    
    private val shaderProgram = ShaderProgram()
    private val mat4 = Matrix4f()
    private val mat3 = Matrix3f()
    private val tempV2 = Vector2f()
    private val tempV3 = Vector3f()
    private val temp2V3 = Vector3f()
    private val tempV4 = Vector4f()
    
    init {
        shaderProgram.createVertexShader(loadResource("/stereographic_simple.vertex.glsl"))
        shaderProgram.createFragmentShader(loadResource("/fragment.glsl"))
        shaderProgram.link()
    }
    
    override fun render(mesh: Mesh, model: Matrix4f, camera: Camera) {
        shaderProgram.bound {
            val scaling = camera.position.length() - 1.175f
            mat3.scaling(1 / scaling, 1 / scaling * camera.aspect, 1f)
            mat3.setUniform(shaderProgram.getUniform("uCamera"))
            mat4.set(mat3.set(camera.right, camera.up, camera.direction).transpose()).mul(model)
            mat4.setUniform(shaderProgram.getUniform("uModel"))
            mesh.draw()
        }
    }
    
    override fun getMouseoverTile(x: Float, y: Float, map: Map, model: Matrix4f,
                                  cameraController: CameraController): FinishedTile? {
        val scaling = cameraController.camera.position.length() - 1.175f
        
        // -y needed to work
        tempV3.set(x, -y, 1f) // Screen Coordinates (result)
        mat3.scaling(1 / scaling, 1 / scaling * cameraController.camera.aspect, 1f) // Camera Scaling matrix
        tempV3.mul(mat3.invert()) // projection result
        val r2 = tempV3.x * tempV3.x + tempV3.y * tempV3.y // circle radius
        tempV3.set(2 * tempV3.x / (1 + r2), 2 * tempV3.y / (1 + r2), (-1 + r2)/(1 + r2)) //Base Sphere location
        mat4.set(mat3.set(cameraController.camera.right, cameraController.camera.up, cameraController.camera.direction).transpose()).mul(model) // Model rotation matrix
        tempV4.set(tempV3.x, tempV3.y, tempV3.z, 1.0f).mul(mat4.invert()) // Rotate onto model
        tempV3.set(tempV4.x / tempV4.w, tempV4.y / tempV4.w, tempV4.z / tempV4.w)
    
        var closest: FinishedTile? = null
        var minDist2 = Float.MAX_VALUE
        for (tile in map.tiles) {
            tile.tilePolygon.getCenter(temp2V3)
            val d2 = temp2V3.sub(tempV3).lengthSquared()
            if (d2 < minDist2) {
                minDist2 = d2
                closest = tile
            }
        }
        return closest
    }
    
    override fun cleanup() {
        shaderProgram.cleanup()
    }
}