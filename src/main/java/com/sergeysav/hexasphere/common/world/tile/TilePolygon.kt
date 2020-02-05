package com.sergeysav.hexasphere.common.world.tile

import org.joml.Vector3fc

/**
 * @author sergeys
 *
 * @constructor Creates a new TilePolygon
 */
data class TilePolygon(val center: Vector3fc, val vertices: Array<Vector3fc>, val norm: Vector3fc) {
    
    val polygonType: TilePolygonType = TilePolygonType.values().first { it.vertices == vertices.size }
    val wedges = vertices.indices.map { TileWedge(center, vertices[(it + 1) % vertices.size], vertices[it], norm) }.toTypedArray()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TilePolygon) return false
        
        if (center != other.center) return false
        if (!vertices.contentEquals(other.vertices)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = center.hashCode()
        result = 31 * result + vertices.contentHashCode()
        return result
    }
}