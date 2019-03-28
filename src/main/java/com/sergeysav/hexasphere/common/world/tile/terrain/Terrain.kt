package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainBaseOutput {
    val name: String
    val baseFood: Int
    val baseProduction: Int
    val baseGold: Int
}

interface TerrainMovementRestrictor {
    val baseMovement: Int
}

abstract class AbstractTerrainBaseOutput(override val name: String,
                                         override val baseFood: Int,
                                         override val baseProduction: Int,
                                         override val baseGold: Int):
        TerrainBaseOutput {
    override fun toString(): String = name
}

abstract class AbstractTerrainMROutput(name: String,
                                       baseFood: Int,
                                       baseProduction: Int,
                                       baseGold: Int,
                                       override val baseMovement: Int):
        AbstractTerrainBaseOutput(name, baseFood, baseProduction, baseGold), TerrainMovementRestrictor
