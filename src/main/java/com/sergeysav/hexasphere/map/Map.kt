package com.sergeysav.hexasphere.map

import com.sergeysav.hexasphere.map.tile.FinishedTile

/**
 * @author sergeys
 *
 * @constructor Creates a new Map
 */
internal typealias KMap<K, V> = kotlin.collections.Map<K, V>

class Map(val tiles: Array<FinishedTile>) {
    val numPentagons = tiles.indexOfFirst { tile -> tile.tilePolygon.polygonType.vertices == 6 }
    val numHexagons = tiles.size - numPentagons
    val numVertices = 5 * numPentagons + 6 * numHexagons
    val numTriangles = (5 - 2) * numPentagons + (6 - 2) * numHexagons
}