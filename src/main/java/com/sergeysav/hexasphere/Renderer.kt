package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import com.sergeysav.hexasphere.map.WorldRenderable
import com.sergeysav.hexasphere.map.tile.Tile

/**
 * @author sergeys
 */
interface Renderer {
    fun render(worldRenderable: WorldRenderable, camera: Camera)
    fun getMouseoverTile(x: Float, y: Float, map: WorldRenderable, cameraController: CameraController): Tile?
    fun cleanup()
}