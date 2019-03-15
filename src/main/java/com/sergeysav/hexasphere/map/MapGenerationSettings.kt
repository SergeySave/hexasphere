package com.sergeysav.hexasphere.map

import com.sergeysav.hexasphere.map.tile.FinishedTile
import com.sergeysav.hexasphere.map.tile.TilePolygon
import com.sergeysav.hexasphere.noiseGenerator
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.random.Random

/**
 * @author sergeys
 *
 * @constructor Creates a new MapGenerationSettings
 */
data class MapGenerationSettings(val size: Int, val plates: Int, val seed: Long, val plateHeightOctaves: Int,
                                 val plateHeightAScale: Float, val plateHeightFScale: Float,
                                 val similarCollisionCoef: Float, val diffRegressionCoef: Float,
                                 val diffCollisionCoef: Float, val seaLevel: Float, val epsilon: Float,
                                 val heatOctaves: Int, val heatAScale: Float,
                                 val heatFScale: Float, val moistureOctaves: Int,
                                 val moistureAScale: Float, val moistureFScale: Float, val biomeOctaves: Int,
                                 val biomeAScale: Float, val biomeFScale: Float) {
    val random: Random = Random(seed)
    val tectonicPlateHeightNoise: (Vector3fc) -> Float = noiseGenerator(random.nextFloat() * 1e4f,
                                                                        octaves = plateHeightOctaves,
                                                                        aScaling = plateHeightAScale,
                                                                        fScaling = plateHeightFScale)
    val heatNoise: (Vector3fc) -> Float = noiseGenerator(random.nextFloat() * 1e4f,
                                                         octaves = heatOctaves,
                                                         aScaling = heatAScale,
                                                         fScaling = heatFScale)
    val moistureNoise: (Vector3fc) -> Float = noiseGenerator(random.nextFloat() * 1e4f,
                                                             octaves = moistureOctaves,
                                                             aScaling = moistureAScale,
                                                             fScaling = moistureFScale)
    val biomeNoise: (Vector3fc) -> Float = noiseGenerator(random.nextFloat() * 1e4f,
                                                          octaves = biomeOctaves,
                                                          aScaling = biomeAScale,
                                                          fScaling = biomeFScale)
}

fun MapGenerationSettings.generate(): Map {
    val map = createBaseMap()
    map.sortBy { tile -> tile.type.vertices }
    val tectonicPlates = generateTectonicPlates(map)
    var elevations = generateElevations(tectonicPlates)
    elevations = erode(elevations)
    val heat = generateHeat(map, elevations)
    val moisture = generateMoisture(map)
    val biomes = generateBiomes(map, elevations, heat, moisture)
    val tilesToPlates = tectonicPlates.flatMap { it.tiles.map { tile -> tile to it } }
            .groupBy { it.first }
            .mapValues { pair -> pair.value.map { it.second }.first() }
    return Map(map.map { baseTile ->
        val v = Vector3f()
        baseTile.getCenter(v)
        val verts = Array<Vector3f>(baseTile.type.vertices) { Vector3f() }
        baseTile.getVertices(verts)
        FinishedTile(TilePolygon(v, verts), tilesToPlates[baseTile]!!, elevations[baseTile]!!, heat[baseTile]!!, moisture[baseTile]!!, biomes[baseTile]!!)
    }.toTypedArray())
}