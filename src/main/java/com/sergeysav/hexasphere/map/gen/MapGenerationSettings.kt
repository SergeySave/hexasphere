package com.sergeysav.hexasphere.map.gen

import com.sergeysav.hexasphere.LinAlgPool
import com.sergeysav.hexasphere.map.World
import com.sergeysav.hexasphere.map.tile.Tile
import com.sergeysav.hexasphere.map.tile.TilePolygon
import com.sergeysav.hexasphere.noiseGenerator
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
                                 val biomeAScale: Float, val biomeFScale: Float, val linAlg: LinAlgPool) {
    
    var seaLevel = 0f
    
    val maxErosionIters = size * 10
    val random: Random = Random(seed)
    val tectonicPlateHeightNoise: (Vector3fc) -> Float = noiseGenerator(random.nextLong(),
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
    elevations = erode(elevations)
    
    log.trace { "Generating Heap Map" }
    val heat = generateHeat(map, elevations)
    
    log.trace { "Generating Moisture Map" }
    val moisture = generateMoisture(map)
    
    log.trace { "Generating Terrain" }
    val terrain = generateTerrain(map, elevations, heat, moisture)
    return World(map.map { baseTile ->
        val v = Vector3f()
        baseTile.getCenter(v)
        val verts = Array(baseTile.type.vertices) { Vector3f() }
        baseTile.getVertices(verts)
        
        var value = PRIMARY_ORIENTATION.dot(verts[0]) // as primary orientation is a unit vector, this is a projection onto it
        var index = 0
        for (i in 1 until verts.size) {
            val thisVal = PRIMARY_ORIENTATION.dot(verts[i])
            if (thisVal > value) {
                value = thisVal
                index = i
            }
        }
        
        val terr = terrain[baseTile]!!
        Tile(TilePolygon(v, Array<Vector3fc>(verts.size) {verts[(it + index) % verts.size]}), elevations[baseTile]!!, heat[baseTile]!!, moisture[baseTile]!!, terr.type, terr.shape, terr.majorFeature, terr.minorFeatures)
    }.toTypedArray())
}