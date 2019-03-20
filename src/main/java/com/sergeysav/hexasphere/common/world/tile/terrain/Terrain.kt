package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainBaseOutput {
    val baseFood: Int
    val baseProduction: Int
    val baseGold: Int
}
abstract class AbstractTerrainBaseOutput(override val baseFood: Int,
                                         override val baseProduction: Int,
                                         override val baseGold: Int):
        TerrainBaseOutput
