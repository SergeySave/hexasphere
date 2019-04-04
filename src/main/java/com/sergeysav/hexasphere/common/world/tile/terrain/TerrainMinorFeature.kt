package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainMinorFeature: TerrainBaseOutput, TerrainMovementRestrictor {
    fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean
    
    object RiverFeature: TerrainMinorFeature, AbstractTerrainMROutput("River", 0, 0, 0, 1) {
        override fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean =
                terrainType is LandTerrainType && terrainShape is LandTerrainShape
    }
}
