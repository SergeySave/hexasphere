package com.sergeysav.hexasphere.map

import com.sergeysav.hexasphere.map.tile.Tile
import org.joml.Vector3f

/**
 * @author sergeys
 *
 * @constructor Creates a new TectonicPlate
 */
class TectonicPlate(val tiles: Set<Tile>, val rotationAxis: Vector3f, val angle: Float, val landPlate: Boolean, val height: Float) {
    val boundaryTiles = tiles.filter { it.adjacent.any { other -> !tiles.contains(other) } }.toSet()
    lateinit var pressures: KMap<Tile, Float>
}