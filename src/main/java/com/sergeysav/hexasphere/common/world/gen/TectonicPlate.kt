package com.sergeysav.hexasphere.common.world.gen

import org.joml.Vector3f

/**
 * @author sergeys
 *
 * @constructor Creates a new TectonicPlate
 */
class TectonicPlate(val tiles: Set<MapGenTile>, val rotationAxis: Vector3f, val angle: Float, val landPlate: Boolean, val height: Float) {
    val boundaryTiles = tiles.filter { it.adjacent.any { other -> !tiles.contains(other) } }.toSet()
    lateinit var pressures: Map<MapGenTile, Float>
}