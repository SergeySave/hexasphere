package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainMinorFeature: TerrainBaseOutput, TerrainMovementRestrictor {
    fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean
}
