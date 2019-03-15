package com.sergeysav.hexasphere.map

import com.sergeysav.hexasphere.map.tile.Tile
import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author sergeys
 *
 * @constructor Creates a new World
 */
class World(val tiles: Array<Tile>) {
    val numPentagons = tiles.indexOfFirst { tile -> tile.tilePolygon.polygonType.vertices == 6 }
    val numHexagons = tiles.size - numPentagons
    val numVertices = 5 * numPentagons + 6 * numHexagons
    val numTriangles = (5 - 2) * numPentagons + (6 - 2) * numHexagons
}

val V = Vector3f()
fun World.getClosestTileTo(vec: Vector3fc) = tiles.minBy { tile ->
    V.set(tile.tilePolygon.center).sub(vec).lengthSquared()
}!!