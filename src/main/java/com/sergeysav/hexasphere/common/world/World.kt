package com.sergeysav.hexasphere.common.world

import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.world.tile.Tile
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

fun World.getClosestTileTo(vec: Vector3fc, linAlgPool: LinAlgPool) = linAlgPool.vec3 { tiles.minBy { tile ->
        it.set(tile.tilePolygon.center).sub(vec).lengthSquared()
    }!!
}