package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainMinorFeature {
    fun compatibleWith(terrainType: TerrainType): Boolean
    
    object NoMinorFeature : TerrainMinorFeature {
        override fun compatibleWith(terrainType: TerrainType): Boolean = true
    }
}
