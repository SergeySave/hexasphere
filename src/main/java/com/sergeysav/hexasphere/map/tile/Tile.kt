package com.sergeysav.hexasphere.map.tile

import org.joml.Vector3f

/**
 * @author sergeys
 *
 * @constructor Creates a new Tile
 */
class Tile(private val center: Vector3f, private val vertices: Array<Vector3f>) {
    
    val type: TileType = TileType.values().first { it.vertices == vertices.size }
    lateinit var adjacent: Array<Tile>
        private set
    
    fun getCenter(vec: Vector3f) {
        vec.set(center)
    }
    
    fun getVertices(store: Array<Vector3f>): Int {
        for (i in 0 until type.vertices) {
            store[i].set(vertices[i])
        }
        return type.vertices
    }
    
    internal fun setAdjacent(adjacent: Array<Tile>) {
        this.adjacent = adjacent
    }
}