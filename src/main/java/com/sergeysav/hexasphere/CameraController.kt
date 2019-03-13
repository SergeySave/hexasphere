package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.gl.Camera
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f

/**
 * @author sergeys
 *
 * @constructor Creates a new CameraController
 */
class CameraController(val camera: Camera) {
    private val tempVec3 = Vector3f()
    private val tempVec4 = Vector4f()
    private val tempMat4 = Matrix4f()
    
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
    
    fun projectToScreen(vec: Vector3f) {
        tempVec4.set(vec.x,  vec.y,  vec.z,  1f)
        tempMat4.set(camera.combined)
        tempVec4.mul(tempMat4)
        vec.x = tempVec4.x / tempVec4.w
        vec.y = tempVec4.y / tempVec4.w
        vec.z = tempVec4.z / tempVec4.w
    }
    
    /**
     * Returns a normalized Vector representing a ray coming out of the camera's position
     * This vector should not be saved as it will be overriden
     */
    fun projectToWorld(vec: Vector2f): Vector3f {
        tempMat4.set(camera.combined)
        tempVec4.set(vec.x, -vec.y, 0f, 1f)
        tempMat4.invert()
        tempVec4.mul(tempMat4)
        return tempVec3.set(tempVec4.x / tempVec4.w, tempVec4.y / tempVec4.w, tempVec4.z / tempVec4.w)
                .sub(camera.position).normalize()
    }
}