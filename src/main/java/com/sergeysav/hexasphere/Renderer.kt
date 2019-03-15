package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.map.World
import com.sergeysav.hexasphere.map.tile.Tile
import org.joml.Matrix4f

/**
 * @author sergeys
 */
interface Renderer {
    fun render(mesh: Mesh, model: Matrix4f, camera: Camera)
    fun getMouseoverTile(x: Float, y: Float, map: World, model: Matrix4f, cameraController: CameraController): Tile?
    fun cleanup()
}