package com.sergeysav.hexasphere.common.world.gen

import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.noiseGenerator
import com.sergeysav.hexasphere.common.world.World
import com.sergeysav.hexasphere.common.world.tile.Tile
import com.sergeysav.hexasphere.common.world.tile.TilePolygon
import mu.KotlinLogging
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * @author sergeys
 *
 * @constructor Creates a new MapGenerationSettings
 */
data class MapGenerationSettings(val size: Int, val plates: Int, val seed: Long, val plateHeightOctaves: Int,
                                 val plateHeightAScale: Float, val plateHeightFScale: Float, val tileHeightOctaves: Int,
                                 val tileHeightAScale: Float, val tileHeightFScale: Float, val tileHeightMult: Float,
                                 val similarCollisionCoef: Float, val diffRegressionCoef: Float,
                                 val diffCollisionCoef: Float, val seaFraction: Double, val epsilon: Float,
                                 val heatOctaves: Int, val heatAScale: Float,
                                 val heatFScale: Float, val moistureOctaves: Int,
                                 val moistureAScale: Float, val moistureFScale: Float, val biomeOctaves: Int,
                                 val biomeAScale: Float, val biomeFScale: Float, val linAlgPool: LinAlgPool,
                                 val numRivers: Int, val minRiverLength: Int = size / 4) {
    
    var seaLevel = 0f
    var erosionIterations = 0
    
    val maxErosionIters = size * 10
    val random: Random = Random(seed)
    val tectonicPlateHeightNoise: (Vector3fc) -> Float = noiseGenerator(
            random.nextLong(),
            octaves = plateHeightOctaves,
            aScaling = plateHeightAScale,
            fScaling = plateHeightFScale)
    val heatNoise: (Vector3fc) -> Float = noiseGenerator(random.nextLong(),
                                                         octaves = heatOctaves,
                                                         aScaling = heatAScale,
                                                         fScaling = heatFScale)
    val moistureNoise: (Vector3fc) -> Float = noiseGenerator(random.nextLong(),
                                                             octaves = moistureOctaves,
                                                             aScaling = moistureAScale,
                                                             fScaling = moistureFScale)
    val biomeNoise: (Vector3fc) -> Float = noiseGenerator(random.nextLong(),
                                                          octaves = biomeOctaves,
                                                          aScaling = biomeAScale,
                                                          fScaling = biomeFScale)
    val tileHeightNoise: (Vector3fc) -> Float = noiseGenerator(random.nextLong(),
                                                               octaves = tileHeightOctaves,
                                                               aScaling = tileHeightAScale,
                                                               fScaling = tileHeightFScale)
}

private val PRIMARY_ORIENTATION: Vector3fc = Vector3f(0f, 1f, 0f)

fun MapGenerationSettings.updateSeaLevel(elevations: Map<MapGenTile, Float>) {
    seaLevel = elevations.values.sorted()[(seaFraction * elevations.size).roundToInt()]
}

private val log = KotlinLogging.logger {}

fun MapGenerationSettings.generate(): World {
    log.info { if (log.isDebugEnabled) "Generating Map using settings $this" else "Generating Map" }
    log.trace { "Generating Base Map" }
    val map = createBaseMap()
    map.sortBy { tile -> tile.type.vertices }
    log.trace { "Generating Tectonic Plates" }
    val tectonicPlates = generateTectonicPlates(map)
    log.trace { "Generating Tile Elevations" }
    var elevations = generateElevations(tectonicPlates)
    
    log.trace { "Renormalizing Sea Level" }
    updateSeaLevel(elevations)
    elevations = elevations.mapValues { (_, elev) -> elev - seaLevel }
    seaLevel = 0f
    
    log.trace { "Simulating Erosion" }
    val (e, riverness) = erode(elevations)
    elevations = e
    
    log.trace { "Generating Heap Map" }
    val heat = generateHeat(map, elevations)
    
    log.trace { "Generating Moisture Map" }
    val moisture = generateMoisture(map)
    
    log.trace { "Generating Terrain" }
    val terrain = generateTerrain(map, elevations, heat, moisture, riverness)
    
//    val scaleFactor = (1/terrain.keys.asSequence().flatMap { it.vertices.asSequence() }.map { it.length() }.average()).toFloat()
    
    val tiles = terrain.mapValues { (baseTile, terr) ->
        val v = Vector3f()
        
        val verts = Array(baseTile.type.vertices) { Vector3f() }
        baseTile.getVertices(verts)
        
        // as primary orientation is a unit vector, this is a projection onto it
        var value = PRIMARY_ORIENTATION.dot(verts[0])
        var index = 0
        for (i in 1 until verts.size) {
            val thisVal = PRIMARY_ORIENTATION.dot(verts[i])
            if (thisVal > value) {
                value = thisVal
                index = i
            }
        }
        val direction = linAlgPool.vec3 { secOrientation ->
            baseTile.getCenter(secOrientation)
            //Protect from NaNs caused by secOrientation being co-linear with PRIMARY_ORIENTATION
            if (secOrientation.normalize().dot(PRIMARY_ORIENTATION) == 1f) return@vec3 -1
            secOrientation.cross(PRIMARY_ORIENTATION).normalize()
            baseTile.getCenter(v)
            val next = v.sub(verts[(index + 1) mod verts.size]).normalize().dot(secOrientation)
            baseTile.getCenter(v)
            val prev = v.sub(verts[(index - 1) mod verts.size]).normalize().dot(secOrientation)
            if (next >= prev) -1 else 1
        }
        
        val tileVerts: Array<Vector3fc> = Array(verts.size) { verts[(direction * it + index) mod verts.size].normalize() }
        v.zero()
        tileVerts.forEach { v.add(it) }
        v.mul(1f/verts.size)
        
        val norm = Vector3f()
        linAlgPool.vec3 { v1 ->
            linAlgPool.vec3 { v2 ->
                tileVerts.indices.forEach {
                    norm.add(v1.set(tileVerts[it]).sub(v).cross(v2.set(tileVerts[(it + 1) % verts.size]).sub(v)).normalize())
                }
            }
        }
    
        Tile(TilePolygon(v, tileVerts, norm.normalize()),
             elevations.getValue(baseTile), heat.getValue(baseTile), moisture.getValue(baseTile),
             terr.type, terr.majorFeature, terr.minorFeatures)
    }
    tiles.forEach { (gen, tile) ->
        tile.setAdjacent(gen.adjacent.mapNotNull(tiles::get).toTypedArray())
    }
    return World(tiles.values.toTypedArray())
}

private infix fun Int.mod(other: Int): Int = if (this >= 0) {
    this % other
} else {
    this % other + other
}
