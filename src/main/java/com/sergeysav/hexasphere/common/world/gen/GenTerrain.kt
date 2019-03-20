package com.sergeysav.hexasphere.common.world.gen

import com.sergeysav.hexasphere.common.world.tile.terrain.IncompatibleTerrainException
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMajorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMinorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainShape
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainType

/**
 * @author sergeys
 *
 * @constructor Creates a new GenTerrain
 */
class GenTerrain(val type: TerrainType,
                 val shape: TerrainShape,
                 val majorFeature: TerrainMajorFeature,
                 val minorFeatures: Array<TerrainMinorFeature>) {
    init {
        if (!shape.compatibleWith(type)) {
            throw IncompatibleTerrainException(
                    "$shape not compatible with $type")
        }
        if (!majorFeature.compatibleWith(type, shape)) {
            throw IncompatibleTerrainException(
                    "$majorFeature not compatible with $type and $shape")
        }
        for (feature in minorFeatures) {
            if (!feature.compatibleWith(type, shape)) {
                throw IncompatibleTerrainException(
                        "$feature not compatible with $type and $shape")
            }
        }
    }
}