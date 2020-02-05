package com.sergeysav.hexasphere.common.world.tile

import org.joml.Matrix4f
import org.joml.Vector3fc

/**
 * @author sergeys
 *
 * @constructor Creates a new Wedge
 */
data class TileWedge(val center: Vector3fc, val vertex1: Vector3fc, val vertex2: Vector3fc, val norm: Vector3fc) {
    fun setMatrix(matrix4f: Matrix4f, radialScale: Float) {
        matrix4f.set(vertex1.x() - center.x(), vertex1.y() - center.y(), vertex1.z() - center.z(), 0f,
                     vertex2.x() - center.x(), vertex2.y() - center.y(), vertex2.z() - center.z(), 0f,
                     -norm.x() * radialScale, -norm.y() * radialScale, -norm.z() * radialScale, 0f,
                     center.x(), center.y(), center.z(), 1f)
    }
}