package com.sergeysav.hexasphere.map.tile

import com.sergeysav.hexasphere.map.Biome

/**
 * @author sergeys
 *
 * @constructor Creates a new Tile
 */
data class Tile(val tilePolygon: TilePolygon,
                val elevation: Float,
                val temperature: Float,
                val moisture: Float,
                val biome: Biome)