package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainType : TerrainBaseOutput {
    object MountainTerrainType: ImpassableTerrainType, AbstractTerrainBaseOutput("Mountain", -100, -100, -100)
    object GrassTerrainType: LandTerrainType, AbstractTerrainBaseOutput("Grass", 1, 0, 0)
    object SandTerrainType: LandTerrainType, AbstractTerrainBaseOutput("Sand", -1, 0, 0)
    object PermafrostTerrainType: LandTerrainType, AbstractTerrainBaseOutput("Snow", 0, 0, 0)
    object WaterTerrainType: SeaTerrainType, AbstractTerrainBaseOutput("Water", 1, 0, 0)
}

interface ImpassableTerrainType: TerrainType
interface LandTerrainType : TerrainType
interface SeaTerrainType : TerrainType
