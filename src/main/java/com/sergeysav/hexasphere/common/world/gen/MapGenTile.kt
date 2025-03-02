package com.sergeysav.hexasphere.common.world.gen

import com.sergeysav.hexasphere.common.world.tile.TilePolygonType
import org.joml.Vector3f

/**
 * @author sergeys
 *
 * @constructor Creates a new MapGenTile
 */
class MapGenTile(private val center: Vector3f, private val vertices: Array<Vector3f>) {
    
    val type: TilePolygonType = TilePolygonType.values().first { it.vertices == vertices.size }
    lateinit var adjacent: Array<MapGenTile>
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
    
    internal fun setAdjacent(adjacent: Array<MapGenTile>) {
        this.adjacent = adjacent
    }
}