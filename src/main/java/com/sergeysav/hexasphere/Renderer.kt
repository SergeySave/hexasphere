package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.Mesh
import org.joml.Matrix4f

/**
 * @author sergeys
 */
interface Renderer {
    fun render(mesh: Mesh, model: Matrix4f, camera: Camera)
    fun cleanup()
}