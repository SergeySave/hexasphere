package com.sergeysav.hexasphere.map.tile

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author sergeys
 *
 * @constructor Creates a new TilePolygon
 */
data class TilePolygon(val center: Vector3fc, val vertices: Array<Vector3fc>) {
    
    val polygonType: TilePolygonType = TilePolygonType.values().first { it.vertices == vertices.size }
    
    @Suppress("UNCHECKED_CAST")
    constructor(center: Vector3fc, vertices: Array<Vector3f>): this(center, vertices as Array<Vector3fc>)
    
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