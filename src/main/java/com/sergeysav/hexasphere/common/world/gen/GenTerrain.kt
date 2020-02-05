package com.sergeysav.hexasphere.common.world.gen

import com.sergeysav.hexasphere.common.world.tile.terrain.IncompatibleTerrainException
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMajorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMinorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainType

/**
 * @author sergeys
 *
 * @constructor Creates a new GenTerrain
 */
class GenTerrain(val type: TerrainType,
                 val majorFeature: TerrainMajorFeature,
                 val minorFeatures: Array<TerrainMinorFeature>) {
    init {
        if (!majorFeature.compatibleWith(type)) {
            throw IncompatibleTerrainException("$majorFeature not compatible with $type")
        }
        for (feature in minorFeatures) {
            if (!feature.compatibleWith(type)) {
                throw IncompatibleTerrainException("$feature not compatible with $type")
            }
        }
    }
}