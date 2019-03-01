package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.ShaderProgram
import com.sergeysav.hexasphere.gl.bound
import org.joml.Matrix4f

/**
 * @author sergeys
 *
 * @constructor Creates a new NormalRenderer
 */
class NormalRenderer : Renderer {
    
    val shaderProgram = ShaderProgram()
    
    init {
        shaderProgram.createVertexShader(loadResource("/vertex.glsl"))
        shaderProgram.createFragmentShader(loadResource("/fragment.glsl"))
        shaderProgram.link()
    }
    
    override fun render(mesh: Mesh, model: Matrix4f, camera: Camera) {
        shaderProgram.bound {
            camera.combined.setUniform(shaderProgram.getUniform("uCamera"))
            model.setUniform(shaderProgram.getUniform("uModel"))
            mesh.draw()
        }
    }
    
    override fun cleanup() {
        shaderProgram.cleanup()
    }
}