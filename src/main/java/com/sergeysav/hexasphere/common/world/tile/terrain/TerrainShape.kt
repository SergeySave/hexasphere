package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainShape : TerrainBaseOutput {
    val baseMovement: Int
    fun compatibleWith(terrainType: TerrainType): Boolean
    
    object FlatTerrainShape: LandTerrainShape, AbstractTerrainShape(1, 0, 0, 1)
    object HillTerrainShape: LandTerrainShape, AbstractTerrainShape(0, 1, 0, 2)
    object MountainTerrainShape: TerrainShape, AbstractTerrainShape(0, 0, 0,
                                                                    100) { //Mountains cannot have outputs and cannot be moved over
        override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is TerrainType.MountainTerrainType
    }
    object CoastTerrainShape: SeaTerrainShape, AbstractTerrainShape(0, 0, 1, 1)
    object OceanTerrainShape: SeaTerrainShape, AbstractTerrainShape(0, 0, 0, 1)
    object IceTerrainShape: SeaTerrainShape, AbstractTerrainShape(0, 0, 0, 1)
}
abstract class AbstractTerrainShape(override val baseFood: Int,
                                    override val baseProduction: Int,
                                    override val baseGold: Int,
                                    override val baseMovement: Int):
        TerrainShape
interface LandTerrainShape : TerrainShape {
    override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is LandTerrainType
}
interface SeaTerrainShape : TerrainShape {
    override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is SeaTerrainType
}
