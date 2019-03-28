package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainShape: TerrainBaseOutput, TerrainMovementRestrictor {
    fun compatibleWith(terrainType: TerrainType): Boolean
    
    object FlatTerrainShape: LandTerrainShape, AbstractTerrainMROutput("Flat", 1, 0, 0, 1)
    object HillTerrainShape: LandTerrainShape, AbstractTerrainMROutput("Hill", 0, 1, 0, 2)
    object MountainTerrainShape: TerrainShape, AbstractTerrainMROutput("Mountain", 0, 0, 0,
                                                                    100) { //Mountains cannot have outputs and cannot be moved over
        override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is TerrainType.MountainTerrainType
    }
    
    object CoastTerrainShape: SeaTerrainShape, AbstractTerrainMROutput("Coast", 0, 0, 1, 1)
    object OceanTerrainShape: SeaTerrainShape, AbstractTerrainMROutput("Ocean", 0, 0, 0, 1)
    object IceTerrainShape: SeaTerrainShape, AbstractTerrainMROutput("Ice", 0, 0, 0, 1)
}
interface LandTerrainShape : TerrainShape {
    override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is LandTerrainType
}
interface SeaTerrainShape : TerrainShape {
    override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is SeaTerrainType
}
