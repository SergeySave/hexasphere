package com.sergeysav.hexasphere.map.tile

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author sergeys
 *
 * @constructor Creates a new TilePolygon
 */
@Suppress("UNCHECKED_CAST")
data class TilePolygon(private val center: Vector3fc, private val vertices: Array<Vector3fc>) {
    
    val polygonType: TileType = TileType.values().first { it.vertices == vertices.size }
    
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
    
    fun getCenter(vec: Vector3f) {
        vec.set(center)
    }
    
    fun getVertices(store: Array<Vector3f>): Int {
        for (i in 0 until polygonType.vertices) {
            store[i].set(vertices[i])
        }
        return polygonType.vertices
    }
}