package com.sergeysav.hexasphere.map.tile.terrain

/**
 * @author sergeys
 */
interface TerrainType : TerrainBaseOutput {
    object GrassTerrainType: LandTerrainType, AbstractTerrainBaseOutput(1, 0, 0)
    object SandTerrainType: LandTerrainType, AbstractTerrainBaseOutput(-1, 0, 0)
    object PermafrostTerrainType: LandTerrainType, AbstractTerrainBaseOutput(0, 0, 0)
    object WaterTerrainType: SeaTerrainType, AbstractTerrainBaseOutput(1, 0, 0)
}
interface LandTerrainType : TerrainType
interface SeaTerrainType : TerrainType
