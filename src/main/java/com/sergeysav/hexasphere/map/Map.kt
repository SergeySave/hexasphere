package com.sergeysav.hexasphere.map

import com.sergeysav.hexasphere.map.tile.Tile

/**
 * @author sergeys
 *
 * @constructor Creates a new Map
 */
internal typealias KMap<K, V> = kotlin.collections.Map<K, V>

class Map(val tiles: Array<Tile>, val plates: Array<TectonicPlate>, private val elevations: KMap<Tile, Float>,
          private val heat: KMap<Tile, Float>,
          private val moisture: KMap<Tile, Float>,
          private val biomes: KMap<Tile, Biome>) {
    val numPentagons = tiles.indexOfFirst { tile -> tile.type.vertices == 6 }
    val numHexagons = tiles.size - numPentagons
    val numVertices = 5 * numPentagons + 6 * numHexagons
    val numTriangles = (5 - 2) * numPentagons + (6 - 2) * numHexagons
    
    val Tile.biome: Biome
        get() = biomes.getValue(this)
}