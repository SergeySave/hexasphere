package com.sergeysav.hexasphere.map.tile

import com.sergeysav.hexasphere.map.Biome
import com.sergeysav.hexasphere.map.TectonicPlate

/**
 * @author sergeys
 *
 * @constructor Creates a new FinishedTile
 */
data class FinishedTile(val tilePolygon: TilePolygon,
                        val plate: TectonicPlate,
                        val elevation: Float,
                        val temperature: Float,
                        val moisture: Float,
                        val biome: Biome) {
}