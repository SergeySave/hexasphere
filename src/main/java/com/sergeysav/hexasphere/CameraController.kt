package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author sergeys
 *
 * @constructor Creates a new CameraController
 */
class CameraController(val camera: Camera) {
    private val tempVec3 = Vector3f()
    
    val forward: Vector3fc
        get() = camera.direction
    val right: Vector3fc
        get() = camera.right
    val up: Vector3fc
        get() = camera.up
    
    fun setAspect(width: Int, height: Int) {
        camera.aspect = width.toFloat() / height
    }
    
    fun setPos(x: Float, y: Float, z: Float) {
        camera.position.set(x, y, z)
    }
    
    fun lookAt(x: Float, y: Float, z: Float) {
        camera.lookAt(tempVec3.set(x, y, z))
    }
    
    fun translate(direction: Vector3fc, amount: Float) {
        camera.position.add(tempVec3.set(direction).mul(amount))
    }
    
    fun rotateAround(center: Vector3fc, axis: Vector3fc, radians: Float) {
        camera.position.set(tempVec3.set(camera.position)
                                    .sub(center)
                                    .rotateAxis(radians, axis.x(), axis.y(),  axis.z())
                                    .add(center))
        camera.lookAt(center)
    }
    
    fun rotate(axis: Vector3fc, radians: Float) {
        camera.rotate(radians, axis)
    }
    
    fun update() {
        camera.update()
    }
}