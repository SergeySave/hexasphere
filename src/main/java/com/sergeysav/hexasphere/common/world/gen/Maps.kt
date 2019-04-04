package com.sergeysav.hexasphere.common.world.gen

import com.sergeysav.hexasphere.common.chance
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMajorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMinorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainShape
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainType
import org.joml.Vector3f
import java.util.LinkedList
import java.util.Queue
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * @author sergeys
 */
private val PHI = (1 + sqrt(5.0)) / 2.0

fun MapGenerationSettings.createBaseMap(): Array<MapGenTile> {
    val faces = createIcosohedron()
    val vertices = splitIcos(size, faces)
    val vertexMap = vertices.groupBy(Vertex::center).mapValues { it.value[0] }
    
    val tiles = getBaseTiles(faces, vertices).toList()
    val tileMap = tiles.groupBy { tile ->
        val center = Vector3f()
        tile.getCenter(center)
        center
    }.mapValues { it.value[0] }
    
    linAlgPool.vec3 { temp ->
        tiles.forEach { tile ->
            tile.getCenter(temp)
            val vertex = vertexMap.getValue(temp)
            tile.setAdjacent(vertex.adjacent.map { tileMap.getValue(it.center) }.toTypedArray())
        }
    }
    
    return tiles.toTypedArray()
}

private fun Random.nextUnitVector(): Vector3f {
    val theta = nextDouble(0.0, 2 * PI)
    val z = nextDouble(-1.0, 1.0)
    val circleRadius = sqrt(1 - z * z)
    return Vector3f((circleRadius * cos(theta)).toFloat(), (circleRadius * sin(theta)).toFloat(), z.toFloat())
}

private fun MapGenerationSettings.loosen(input: Map<MapGenTile, Int>) =
    linAlgPool.vec3 { vec1 ->
        linAlgPool.vec3 { vec2 ->
            val tPlates = mutableMapOf<MapGenTile, Int>()
            val queue: Queue<MapGenTile> = LinkedList<MapGenTile>()
            input.entries.groupBy { it.value }.forEach {
                val plateTiles = it.value.asSequence().map { tile -> tile.key }
                vec1.set(0f, 0f, 0f)
                plateTiles.forEach { tile ->
                    tile.getCenter(vec2)
                    vec1.add(vec2)
                }
                vec1.mul(1f/it.value.size)
                //Get the closest tile to the center (average)
                val closest = plateTiles.minBy { tile ->
                    tile.getCenter(vec2)
                    vec2.sub(vec1).lengthSquared()
                }!!
                tPlates[closest] = it.key
                queue.offer(closest)
            }
    
            while (queue.isNotEmpty()) {
                val tile = queue.poll()
                val plate = tPlates[tile]!!
        
                for (adjacent in tile.adjacent) {
                    if (!tPlates.containsKey(adjacent)) {
                        tPlates[adjacent] = plate
                        queue.offer(adjacent)
                    }
                }
            }
            tPlates
        }
    }

fun MapGenerationSettings.generateTectonicPlates(map: Array<MapGenTile>): Array<TectonicPlate> {
    var tPlates = mutableMapOf<MapGenTile, Int>()

    for (i in 0 until plates) {
        var tileIndex: Int
        do {
            tileIndex = random.nextInt(map.size)
        } while (tPlates.containsKey(map[tileIndex]))
        tPlates[map[tileIndex]] = i
    }
    tPlates = loosen(tPlates) //Fill out the tPlates
    
    for (i in 0 until 1) {
        tPlates = loosen(tPlates) //Loosen the plates
    }
    
    return linAlgPool.vec3 { vec1 ->
        linAlgPool.vec3 { vec2 ->
            val plateToVals = tPlates.entries.groupBy { it.value }.mapValues {
                val plateTiles = it.value.map { tile -> tile.key }.toSet()
                vec2.zero()
                plateTiles.forEach { tile ->
                    tile.getCenter(vec1)
                    vec2.add(vec1)
                }
                vec2.mul(1f/plateTiles.size)
                tectonicPlateHeightNoise(vec2) to plateTiles
            }
            val quantities = plateToVals.entries.sortedBy { it.value.first }.map { it.key to it.value.second.size / map.size.toDouble() }.toMutableList()
            var resultI = 0
            for (i in 1 until quantities.size) {
                quantities[i] = quantities[i].first to (quantities[i].second + quantities[i - 1].second)
                if (quantities[i].second < seaFraction) {
                    resultI = i
                }
            }
            val noiseCutoff = plateToVals.getValue(quantities[resultI].first).first //+ plateToVals.getValue(quantities[resultI + 1].first).first) / 2
            
            val tectonicPlates = tPlates.entries.groupBy { it.value }
                    .map {
                        val angle = (random.nextFloat() * 2 * PI / 180).toFloat()
                        val plateTiles = plateToVals.getValue(it.key).second
                        val noiseVal = plateToVals.getValue(it.key).first
                        val landPlate = noiseVal > noiseCutoff
                        var height = noiseVal * 0.5f
                        height = if (landPlate) {
                            0.1f + height
                        } else {
                            -0.1f - height
                        }
                        TectonicPlate(plateTiles, random.nextUnitVector(),
                                      angle, landPlate,
                                      height)
                    }.toTypedArray()
    
            val tilesToPlates = tectonicPlates.flatMap { it.tiles.map { tile -> tile to it } }
                    .groupBy { it.first }
                    .mapValues { pair -> pair.value.map { it.second }.first() }
    
            linAlgPool.vec3 { vec3 ->
                linAlgPool.vec3 { vec4 ->
                    for (plate in tectonicPlates) {
                        plate.pressures = plate.boundaryTiles.associateWith {
                            it.getCenter(vec1)
                            it.getCenter(vec2)
                            //vec1 = desired position
                            vec1.rotateAxis(plate.angle, plate.rotationAxis.x, plate.rotationAxis.y, plate.rotationAxis.z)
                            //vec 1 = tile "velocity"
                            vec1.sub(vec2)
                            //adjacent tiles in other plates
                            it.adjacent.filter { other -> !plate.tiles.contains(other) }
                                    //get the pressure from each adjacent tile
                                    .map { other ->
                                        val otherPlate = tilesToPlates.getValue(other)
                                        other.getCenter(vec3)
                                        other.getCenter(vec4)
                                        //vec3 = desired position
                                        vec3.rotateAxis(otherPlate.angle, otherPlate.rotationAxis.x, otherPlate.rotationAxis.y,
                                                        otherPlate.rotationAxis.z)
                                        //vec3 = tile "velocity"
                                        vec3.sub(vec4)
                        
                                        //vec3 = relative "velocity"
                                        vec3.sub(vec1) //Difference in motions from other's point of view
                        
                                        vec4.sub(vec2).normalize() //Direction vector towards original tile from other's point of view
                                        vec3.dot(vec4) // relative "velocity" projected onto the vector pointing towards the original tile
                                        //this returns an inward pressure
                                    }.sum() // The total pressure exerted into this tile
                        }
        
                        //Basic smoothing to ensure that diagonal lines don't look really weird
                        plate.pressures = plate.pressures.mapValues { (tile, pressure) ->
                            pressure * 0.5f + tile.adjacent
                                    //adjacent boundary tiles in the same plate
                                    .filter { plate.boundaryTiles.contains(it) }
                                    //pressure of the other tile
                                    .map { plate.pressures.getValue(it) }
                                    //maximum adjacent boundary pressure
                                    .max()!!.toFloat() * 0.5f
                        }
                    }
                }
            }
            tectonicPlates
        }
    }
}

fun MapGenerationSettings.generateElevations(plates: Array<TectonicPlate>): Map<MapGenTile, Float> {
    val elevations = mutableMapOf<MapGenTile, Float>()
    val tilesToPlates = plates.flatMap { it.tiles.map { tile -> tile to it } }
            .groupBy { it.first }
            .mapValues { pair -> pair.value.map { it.second }.first() }
    
    plates.map { plate -> plate.tiles.associateWith { plate.height } }.forEach { elevations.putAll(it) }
    
    for (plate in plates) {
        val innerLWBAdj = mutableSetOf<MapGenTile>()
        for (tile in plate.boundaryTiles) {
            val pressure = plate.pressures.getValue(tile)
            // Adjacent tiles in other plates
            val otherPlates = tile.adjacent.filter { !plate.tiles.contains(it) }
            val pressurePerPlate = pressure/otherPlates.size
            elevations[tile] = elevations[tile]!! + otherPlates.map { other ->
                val otherPlate = tilesToPlates.getValue(other)
                if (plate.landPlate == otherPlate.landPlate) { //both land or both water
                    pressurePerPlate * similarCollisionCoef
                } else {
                    if (pressure > 0) {
                        innerLWBAdj.addAll(tile.adjacent.filter { plate.tiles.contains(it) && !plate.boundaryTiles.contains(it) })
                        min(pressurePerPlate * diffRegressionCoef, 0.5f) * (elevations[other]!! - elevations[tile]!!)
                    } else {
                        pressurePerPlate * similarCollisionCoef
                    }
                }
            }.sum()
        }
    
        for (innerTile in innerLWBAdj) {
            val pressure = innerTile.adjacent.filter { plate.boundaryTiles.contains(it) }
                    .map { plate.pressures.getValue(it) }
                    .max()!!
            elevations[innerTile] = elevations[innerTile]!! + if (plate.landPlate) {
                diffCollisionCoef * pressure
            } else {
                -diffCollisionCoef * pressure
            }
        }
    }
    
    return linAlgPool.vec3 { v3 ->
        elevations.mapValues {
            it.key.getCenter(v3)
            it.value + tileHeightNoise(v3) * tileHeightMult
        }
    }
}

// erosion
// Everything lower than a certain point is ocean (deeper than normal coast tiles)
// ocean tiles are the edge tiles
//  elevation for ocean tiles = original elevation for ocean tiles
//  elevation = INFINITY for all other tiles
//  repeat until no changes have been made:
//   find tiles where min(adjacent elevation) < elevation
//   for those tiles set elevation = min(adjacent elevation) + epsilon
fun MapGenerationSettings.erode(
        elevations: Map<MapGenTile, Float>): Pair<Map<MapGenTile, Float>, Map<MapGenTile, Float>> {
    var elevation = elevations.mapValues { (_, value) ->
        if (value < seaLevel) {
            value
        } else {
            Float.POSITIVE_INFINITY
        }
    }
    var riverness = elevations.mapValues { 0f }.toMutableMap()
    
    var iterations = 0
    do {
        var changed = false
    
        val newRiverness = elevations.mapValues { 0f }.toMutableMap()
        elevation = elevation.mapValues { (tile, value) ->
            if (value < seaLevel) {
                return@mapValues value
            }
            var newValue = value
            val lowestAdjacent = tile.adjacent.minBy { elevation.getValue(it) }!!
            val minElevation = elevation.getValue(lowestAdjacent)
            if (newValue > elevations.getValue(tile)) {
                newValue = elevations.getValue(tile)
            }
            if (newValue < minElevation) {
                newValue = minElevation + epsilon
                //                newRiverness[lowestAdjacent] = riverness[lowestAdjacent]!! + riverness[tile]!! + 1
            }
            newRiverness[lowestAdjacent] = newRiverness[lowestAdjacent]!! + riverness[tile]!! * 3 / 4f + 1
            if (value != newValue) {
                changed = true
            }
            newValue
        }
        riverness = newRiverness
        iterations++
    } while (changed && iterations < maxErosionIters)
    
    erosionIterations = iterations
    
    return elevation to riverness
}

fun MapGenerationSettings.generateHeat(map: Array<MapGenTile>, elevations: Map<MapGenTile, Float>): Map<MapGenTile, Float> {
    val tEquator = 1.0
    val tPole = 0.0
    val tLat = { cosTheta: Float -> (tEquator - tPole) * cosTheta + tPole}
    
    
    val heat = mutableMapOf<MapGenTile, Float>()
    
    linAlgPool.vec3 { vec1 ->
        map.forEach { tile ->
            tile.getCenter(vec1)
            val dot = vec1.normalize().dot(0f, 1f, 0f)
            //a.b = |a||b|cos(theta)
        
            //sin
            // this is the sin compared to the top
            val sin = sqrt(1 - dot * dot)
        
            tile.getCenter(vec1)
            val height = elevations.getValue(tile)
        
            heat[tile] = tLat(sin).toFloat() + heatNoise(vec1)/12 - (if (height < 0) 0f else height/5)
        }
    }
    
    return heat
}

fun MapGenerationSettings.generateMoisture(map: Array<MapGenTile>): Map<MapGenTile, Float> {
    val moisture = mutableMapOf<MapGenTile, Float>()

    linAlgPool.vec3 { vec1 ->
        map.forEach {
            it.getCenter(vec1)
            val dot = vec1.normalize().dot(0f, 1f, 0f)
            val theta = asin(dot)
            it.getCenter(vec1)
            moisture[it] = ((1 / (1 + (4 * theta) * (4 * theta)) + 0.5 / (1 + (4 * (theta - PI / 4)) * (4 * (theta - PI / 4))) + 0.5 / (1 + (4 * (theta + PI / 4)) * (4 * (theta + PI / 4))) - 0.0763) / 1.0157).toFloat() + moistureNoise(vec1) / 6
        }
    }
    
    return moisture
}

fun MapGenerationSettings.generateRivers(elevations: Map<MapGenTile, Float>, riverness: Map<MapGenTile, Float>,
                                         moisture: Map<MapGenTile, Float>): Map<MapGenTile, Boolean> {
    val passes = riverness.mapValues { (tile, r) -> r * moisture.getValue(tile) * moisture.getValue(tile) }
            .filter { (_, r) -> r > 0 }
            .filter { (tile, _) -> elevations.getValue(tile) > 0 }.toMutableMap()
    var rSum = passes.values.sum().toDouble()
    val rivers = riverness.mapValues { false }.toMutableMap()
    var i = 0
    while (i < numRivers && rSum > 0 && passes.isNotEmpty()) {
        var startVal = random.nextDouble(rSum)
        var river = passes.keys.first()
        for ((tile, r) in passes) {
            startVal -= r
            if (startVal <= 0) {
                river = tile
                break
            }
        }
        // create river from riverStart
        // run a river until it reaches the ocean or another river
        val newRiver = mutableListOf<MapGenTile>()
        while (elevations.getValue(river) > 0 && !rivers.getValue(river)) {
            //Set this tile as a river tile and update counter things
            newRiver.add(river)
            if (passes.containsKey(river)) {
                rSum -= passes.getValue(river)
            }
            passes.remove(river)
            
            //Move downstream
            val lowestAdjacent = river.adjacent.minBy {
                elevations.getValue(it) - if (rivers.getValue(it)) 10000 else 0
            }!!
            river = lowestAdjacent
        }
        if (newRiver.size >= minRiverLength) {
            newRiver.forEach { rivers[it] = true }
            i++
        }
    }
    return rivers
}

fun MapGenerationSettings.generateTerrain(map: Array<MapGenTile>,
                                          elevations: Map<MapGenTile, Float>,
                                          temperatures: Map<MapGenTile, Float>,
                                          moisture: Map<MapGenTile, Float>,
                                          riverness: Map<MapGenTile, Float>): Map<MapGenTile, GenTerrain> {
    val terrain = mutableMapOf<MapGenTile, GenTerrain>()
    val rivers = generateRivers(elevations, riverness, moisture)
    linAlgPool.vec3 { vec ->
        map.forEach {
            val h = elevations.getValue(it)
            val t = temperatures.getValue(it)
            val m = moisture.getValue(it)
            
            it.getCenter(vec)
            val n = biomeNoise(vec)
            //        println(n)
            val smallerCount = it.adjacent.count { adj -> elevations.getValue(adj) <= h }.toDouble() / it.adjacent.size
            val isCoastal = it.adjacent.any { adj -> elevations.getValue(adj) >= 0 }
            val isCoastal2 = it.adjacent.any { adj -> adj.adjacent.any { adj2 -> elevations.getValue(adj2) >= 0 } }
            //            val riverStuffs = it.adjacent.map { adj -> riverness.getValue(adj) }.sortedDescending()
    
            val minorFeatures = mutableListOf<TerrainMinorFeature>()
    
            if (rivers.getValue(it)) {
                minorFeatures.add(TerrainMinorFeature.RiverFeature)
            }
    
            terrain[it] = if (h < 0) {
                when {
                    t < 0.25                                                        -> GenTerrain(
                            TerrainType.WaterTerrainType,
                            TerrainShape.IceTerrainShape,
                            TerrainMajorFeature.NoMajorFeature,
                            arrayOf())
                    h > (-0.015 + n / 15) || isCoastal || (isCoastal2 && n > -0.15) -> GenTerrain(
                            TerrainType.WaterTerrainType,
                            TerrainShape.CoastTerrainShape,
                            TerrainMajorFeature.NoMajorFeature,
                            arrayOf())
                    else                                                            -> GenTerrain(
                            TerrainType.WaterTerrainType,
                            TerrainShape.OceanTerrainShape,
                            TerrainMajorFeature.NoMajorFeature,
                            arrayOf())
                }
            } else if (smallerCount + n / 2 - h / 2 < 1.4 / 9 && !minorFeatures.contains(
                            TerrainMinorFeature.RiverFeature)) {
                // Can be any Land Type
                GenTerrain(
                        TerrainType.MountainTerrainType,
                        TerrainShape.MountainTerrainShape,
                        TerrainMajorFeature.NoMajorFeature,
                        arrayOf())
            } else if (0.85 * t < m && t > 0.5) {
                when {
                    random.chance(1 / 5.0) -> GenTerrain(
                            TerrainType.GrassTerrainType,
                            TerrainShape.HillTerrainShape,
                            TerrainMajorFeature.NoMajorFeature,
                            minorFeatures.toTypedArray())
                    random.chance(1 / 4.0) -> GenTerrain(
                            TerrainType.GrassTerrainType,
                            TerrainShape.FlatTerrainShape,
                            TerrainMajorFeature.NoMajorFeature,
                            minorFeatures.toTypedArray())
                    else                   -> GenTerrain(
                            TerrainType.GrassTerrainType,
                            TerrainShape.FlatTerrainShape,
                            TerrainMajorFeature.RainforestMajorFeature,
                            minorFeatures.toTypedArray())
                }
            } else if (0.45 * t < m && t > 0.4) {
                // Can be any Land Type
                when {
                    random.chance(1 / 5.0) -> GenTerrain(
                            TerrainType.GrassTerrainType,
                            TerrainShape.HillTerrainShape,
                            TerrainMajorFeature.NoMajorFeature,
                            minorFeatures.toTypedArray())
                    random.chance(1 / 4.0) -> GenTerrain(
                            TerrainType.GrassTerrainType,
                            TerrainShape.FlatTerrainShape,
                            TerrainMajorFeature.NoMajorFeature,
                            minorFeatures.toTypedArray())
                    else                   -> GenTerrain(
                            TerrainType.GrassTerrainType,
                            TerrainShape.FlatTerrainShape,
                            TerrainMajorFeature.ForestMajorFeature,
                            minorFeatures.toTypedArray())
                }
            } else if (0.35 * t < m) {
                if (t > 0.5) {
                    GenTerrain(
                            TerrainType.SandTerrainType,
                            if (random.chance(0.5)) {
                                TerrainShape.FlatTerrainShape
                            } else {
                                TerrainShape.HillTerrainShape
                            },
                            TerrainMajorFeature.NoMajorFeature,
                            minorFeatures.toTypedArray())
                } else {
                    GenTerrain(
                            TerrainType.PermafrostTerrainType,
                            if (random.chance(0.5)) {
                                TerrainShape.FlatTerrainShape
                            } else {
                                TerrainShape.HillTerrainShape
                            },
                            TerrainMajorFeature.NoMajorFeature,
                            minorFeatures.toTypedArray())
                }
            } else {
                if (t > 0.4) {
                    GenTerrain(
                            TerrainType.SandTerrainType,
                            TerrainShape.FlatTerrainShape,
                            TerrainMajorFeature.ForestMajorFeature,
                            minorFeatures.toTypedArray())
                } else {
                    GenTerrain(
                            TerrainType.PermafrostTerrainType,
                            TerrainShape.FlatTerrainShape,
                            TerrainMajorFeature.ForestMajorFeature,
                            minorFeatures.toTypedArray())
                }
            }
        }
    }
    return terrain
}

private fun getIcosVertices(): Array<Vertex> {
    val one = (1 * 1).toFloat()
    val phi = (PHI * one).toFloat()
    
    return arrayOf(Vertex(Vector3f(0f, one, phi)),
                   Vertex(Vector3f(0f, -one, phi)),
                   Vertex(Vector3f(phi, 0f, one)),
                   Vertex(Vector3f(-phi, 0f, one)),
                   Vertex(Vector3f(0f, one, -phi)),
                   Vertex(Vector3f(0f, -one, -phi)),
                   Vertex(Vector3f(phi, 0f, -one)),
                   Vertex(Vector3f(-phi, 0f, -one)),
                   Vertex(Vector3f(one, phi, 0f)),
                   Vertex(Vector3f(one, -phi, 0f)),
                   Vertex(Vector3f(-one, phi, 0f)),
                   Vertex(Vector3f(-one, -phi, 0f)))
}

private fun createIcosohedron(): Array<Face> {
    val vertices = getIcosVertices()
    
    connect(vertices[0], vertices[1])
    connect(vertices[0], vertices[2])
    connect(vertices[0], vertices[3])
    connect(vertices[0], vertices[8])
    connect(vertices[0], vertices[10])
    connect(vertices[1], vertices[2])
    connect(vertices[1], vertices[3])
    connect(vertices[1], vertices[9])
    connect(vertices[1], vertices[11])
    connect(vertices[2], vertices[6])
    connect(vertices[2], vertices[8])
    connect(vertices[2], vertices[9])
    connect(vertices[3], vertices[7])
    connect(vertices[3], vertices[10])
    connect(vertices[3], vertices[11])
    connect(vertices[4], vertices[5])
    connect(vertices[4], vertices[6])
    connect(vertices[4], vertices[7])
    connect(vertices[4], vertices[8])
    connect(vertices[4], vertices[10])
    connect(vertices[5], vertices[6])
    connect(vertices[5], vertices[7])
    connect(vertices[5], vertices[9])
    connect(vertices[5], vertices[11])
    connect(vertices[6], vertices[8])
    connect(vertices[6], vertices[9])
    connect(vertices[7], vertices[10])
    connect(vertices[7], vertices[11])
    connect(vertices[8], vertices[10])
    connect(vertices[9], vertices[11])
    
    return arrayOf(
            Face(vertices[0], vertices[1], vertices[2]),
            Face(vertices[0], vertices[1], vertices[3]),
            Face(vertices[0], vertices[2], vertices[8]),
            Face(vertices[0], vertices[3], vertices[10]),
            Face(vertices[0], vertices[8], vertices[10]),
        
            Face(vertices[6], vertices[2], vertices[8]),
            Face(vertices[6], vertices[4], vertices[8]),
            Face(vertices[6], vertices[4], vertices[5]),
            Face(vertices[6], vertices[5], vertices[9]),
            Face(vertices[6], vertices[2], vertices[9]),
        
            Face(vertices[1], vertices[2], vertices[9]),
            Face(vertices[4], vertices[8], vertices[10]),
            Face(vertices[3], vertices[7], vertices[10]),
            Face(vertices[4], vertices[7], vertices[10]),
            Face(vertices[4], vertices[5], vertices[7]),
        
            Face(vertices[1], vertices[9], vertices[11]),
            Face(vertices[1], vertices[3], vertices[11]),
            Face(vertices[3], vertices[7], vertices[11]),
            Face(vertices[5], vertices[7], vertices[11]),
            Face(vertices[5], vertices[9], vertices[11])
    )
}

private fun splitIcos(size: Int, faces: Array<Face>): Set<Vertex> {
    val len = faces[0].v1.center.length()
    split(faces, size)
    
    val set = mutableSetOf<Vertex>()
    findAllVerticies(faces[0].v1, set)
    
    set.asSequence().map(
            Vertex::center).forEach { vertex -> vertex.normalize(len) }
    
    return set
}

private fun getBaseTiles(faces: Array<Face>, vertices: Set<Vertex>): Sequence<MapGenTile> {
    val oneThird = 1f/3
    
    val pentagonalTiles = faces.asSequence()
            .flatMap { sequenceOf(it.v1, it.v2, it.v3) }
            .distinct()
            .map { vertex ->
                val a = vertex.adjacent[0]
                val b = firstMutualNeighbor(vertex, a, null)
                val c = firstMutualNeighbor(vertex, b, a)
                val d = firstMutualNeighbor(vertex, c, b)
                val e = firstMutualNeighbor(vertex, d, c)
                MapGenTile(vertex.center,
                           arrayOf(Vector3f(a.center).add(b.center).add(
                                   vertex.center).mul(oneThird),
                                   Vector3f(b.center).add(c.center).add(
                                           vertex.center).mul(oneThird),
                                   Vector3f(c.center).add(d.center).add(
                                           vertex.center).mul(oneThird),
                                   Vector3f(d.center).add(e.center).add(
                                           vertex.center).mul(oneThird),
                                   Vector3f(e.center).add(a.center).add(
                                           vertex.center).mul(
                                           oneThird)))
            }
    
    val hexagonalTiles = vertices.asSequence().filter { it.adjacent.size == 6 }
            .map { vertex ->
                val a = vertex.adjacent[0]
                val b = firstMutualNeighbor(vertex, a, null)
                val c = firstMutualNeighbor(vertex, b, a)
                val d = firstMutualNeighbor(vertex, c, b)
                val e = firstMutualNeighbor(vertex, d, c)
                val f = firstMutualNeighbor(vertex, e, d)
                MapGenTile(vertex.center,
                           arrayOf(Vector3f(a.center).add(b.center).add(
                                   vertex.center).mul(oneThird),
                                   Vector3f(b.center).add(c.center).add(
                                           vertex.center).mul(oneThird),
                                   Vector3f(c.center).add(d.center).add(
                                           vertex.center).mul(oneThird),
                                   Vector3f(d.center).add(e.center).add(
                                           vertex.center).mul(oneThird),
                                   Vector3f(e.center).add(f.center).add(
                                           vertex.center).mul(oneThird),
                                   Vector3f(f.center).add(a.center).add(
                                           vertex.center).mul(
                                           oneThird)))
            }
    
    return sequenceOf(pentagonalTiles, hexagonalTiles).flatten()
}

private fun split(faces: Array<Face>, size: Int) {
    for ((v1, v2, v3) in faces) {
        //Ensure borders exist
        if (v1.adjacent.contains(v2)) {
            disconnect(v1, v2)
            var last: Vertex = v1
            val path = mutableListOf<Vertex>()
            for (i in 0 until size) {
                val new = Vertex(
                        Vector3f(v1.center).lerp(v2.center, (i + 1) / (size + 1).toFloat()))
                path.add(new)
                connect(last, new)
                last = new
            }
            connect(last, v2)
            path.add(v2)
            v1.majorPaths.add(path)
            val reversed = ArrayList(path)
            reversed.removeAt(reversed.size - 1)
            reversed.reverse()
            reversed.add(v1)
            v2.majorPaths.add(reversed)
        }
        if (v1.adjacent.contains(v3)) {
            disconnect(v1, v3)
            var last: Vertex = v1
            val path = mutableListOf<Vertex>()
            for (i in 0 until size) {
                val new = Vertex(
                        Vector3f(v1.center).lerp(v3.center, (i + 1) / (size + 1).toFloat()))
                path.add(new)
                connect(last, new)
                last = new
            }
            connect(last, v3)
            path.add(v3)
            v1.majorPaths.add(path)
            val reversed = ArrayList(path)
            reversed.removeAt(reversed.size - 1)
            reversed.reverse()
            reversed.add(v1)
            v3.majorPaths.add(reversed)
        }
        if (v2.adjacent.contains(v3)) {
            disconnect(v2, v3)
            var last: Vertex = v2
            val path = mutableListOf<Vertex>()
            for (i in 0 until size) {
                val new = Vertex(
                        Vector3f(v2.center).lerp(v3.center, (i + 1) / (size + 1).toFloat()))
                path.add(new)
                connect(last, new)
                last = new
            }
            connect(last, v3)
            path.add(v3)
            v2.majorPaths.add(path)
            val reversed = ArrayList(path)
            reversed.removeAt(reversed.size - 1)
            reversed.reverse()
            reversed.add(v2)
            v3.majorPaths.add(reversed)
        }
        
        //Fill in the center of the section
        val pathBottom = v1.majorPaths.first { it.last() == v3 }
        val pathTop = v2.majorPaths.first { it.last() == v3 }
        var previous = v1.majorPaths.first { it.last() == v2 }
        for (i in 0 until size) {
            val bottom = pathBottom[i]
            var last = bottom
            val top = pathTop[i]
            val thisOne = ArrayList<Vertex>()
            for (j in 0 until (size - i - 1)) {
                connect(last, previous[j])
                val new = Vertex(
                        Vector3f(bottom.center).lerp(top.center, (j + 1) / (size - i).toFloat()))
                connect(last, new)
                connect(new, previous[j])
                thisOne.add(new)
                last = new
            }
            connect(last, previous[size - i - 1])
            connect(last, top)
            connect(top, previous[size - i - 1])
            previous = thisOne
        }
    }
}

private fun firstMutualNeighbor(v1: Vertex, v2: Vertex, not: Vertex?) = v1.adjacent.intersect(v2.adjacent).first { it != not }

private fun findAllVerticies(vertex: Vertex, set: MutableSet<Vertex>) {
    set.add(vertex)
    for (adjacent in vertex.adjacent) {
        if (!set.contains(adjacent)) {
            findAllVerticies(adjacent, set)
        }
    }
}

private class Vertex(val center: Vector3f,
                     val majorPaths: MutableList<List<Vertex>> = mutableListOf()) {
    val adjacent: MutableList<Vertex> = mutableListOf()
}

private data class Face(val v1: Vertex, val v2: Vertex, val v3: Vertex)

private fun connect(v1: Vertex, v2: Vertex) {
    v1.adjacent.add(v2)
    v2.adjacent.add(v1)
}
private fun disconnect(v1: Vertex, v2: Vertex) {
    v1.adjacent.remove(v2)
    v2.adjacent.remove(v1)
}

