package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainType : TerrainBaseOutput {
    object MountainTerrainType: ImpassableTerrainType, AbstractTerrainBaseOutput(-100, -100, -100)
    object GrassTerrainType: LandTerrainType, AbstractTerrainBaseOutput(1, 0, 0)
    object SandTerrainType: LandTerrainType, AbstractTerrainBaseOutput(-1, 0, 0)
    object PermafrostTerrainType: LandTerrainType, AbstractTerrainBaseOutput(0, 0, 0)
    object WaterTerrainType: SeaTerrainType, AbstractTerrainBaseOutput(1, 0, 0)
}

interface ImpassableTerrainType: TerrainType
interface LandTerrainType : TerrainType
interface SeaTerrainType : TerrainType
