package com.sergeysav.hexasphere.map.tile.terrain

/**
 * @author sergeys
 */
interface TerrainMinorFeature : TerrainBaseOutput {
    fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean
}
