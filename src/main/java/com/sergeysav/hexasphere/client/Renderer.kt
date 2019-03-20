package com.sergeysav.hexasphere.client

import com.sergeysav.hexasphere.client.camera.Camera
import com.sergeysav.hexasphere.client.camera.CameraController
import com.sergeysav.hexasphere.client.world.WorldRenderable
import com.sergeysav.hexasphere.common.world.tile.Tile

/**
 * @author sergeys
 */
interface Renderer {
    fun render(worldRenderable: WorldRenderable, camera: Camera)
    fun getMouseoverTile(x: Float, y: Float, map: WorldRenderable, cameraController: CameraController): Tile?
    fun cleanup()
}