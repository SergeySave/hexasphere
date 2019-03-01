package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.ShaderProgram
import com.sergeysav.hexasphere.gl.bound
import org.joml.Matrix3f
import org.joml.Matrix4f

/**
 * @author sergeys
 *
 * @constructor Creates a new NormalRenderer
 */
class SimpleStereographicRenderer : Renderer {
    
    private val shaderProgram = ShaderProgram()
    private val mat4 = Matrix4f()
    private val mat3 = Matrix3f()
    
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
    
    override fun cleanup() {
        shaderProgram.cleanup()
    }
}