package com.sergeysav.hexasphere.map.gen

import com.sergeysav.hexasphere.map.Biome
import org.joml.SimplexNoise
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
            val vertex = vertexMap[temp]!!
            tile.setAdjacent(vertex.adjacent.map { tileMap[it.center]!! }.toTypedArray())
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
                val plateTiles = it.value.asSequence().map { it.key }
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
    
    //Flip the tPlates variable around
    val tectonicPlates = linAlgPool.vec3 { vec1 ->
        linAlgPool.vec3 { vec2 ->
            val tectonicPlates = tPlates.entries.groupBy { it.value }
                    .map {
                        val angle = (random.nextFloat() * 2 * PI / 180).toFloat()
                        val plateTiles = it.value.map { it.key }.toSet()
                        vec2.zero()
                        plateTiles.forEach { tile ->
                            tile.getCenter(vec1)
                            vec2.add(vec1)
                        }
                        vec2.mul(1f/plateTiles.size)
                        val noiseVal = tectonicPlateHeightNoise(vec2)
                        val landPlate = noiseVal > 0
                        var height = noiseVal * 0.5f
                        height = if (landPlate) {
                            0.1f + height
                        } else {
                            -0.1f - height
                        }
                        TectonicPlate(plateTiles, random.nextUnitVector(), angle, landPlate,
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
                                        val otherPlate = tilesToPlates[other]!!
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
                                    .map { plate.pressures[it]!! }
                                    //maximum adjacent boundary pressure
                                    .max()!!.toFloat() * 0.5f
                        }
                    }
                }
            }
            tectonicPlates
        }
    }
    
    return tectonicPlates
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
            val pressure = plate.pressures[tile]!!
            // Adjacent tiles in other plates
            val otherPlates = tile.adjacent.filter { !plate.tiles.contains(it) }
            val pressurePerPlate = pressure/otherPlates.size
            elevations[tile] = elevations[tile]!! + otherPlates.map { other ->
                val otherPlate = tilesToPlates[other]!!
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
                    .map { plate.pressures[it]!! }
                    .max()!!
            elevations[innerTile] = elevations[innerTile]!! + if (plate.landPlate) {
                diffCollisionCoef * pressure
            } else {
                -diffCollisionCoef * pressure
            }
        }
    }
    
    return elevations
}

// erosion
// Everything lower than a certain point is ocean (deeper than normal coast tiles)
// ocean tiles are the edge tiles
//  elevation for ocean tiles = original elevation for ocean tiles
//  elevation = INFINITY for all other tiles
//  repeat until no changes have been made:
//   find tiles where min(adjacent elevation) < elevation
//   for those tiles set elevation = min(adjacent elevation) + epsilon
fun MapGenerationSettings.erode(elevations: Map<MapGenTile, Float>): Map<MapGenTile, Float> {
    var elevation = elevations.mapValues { (_, value) ->
        if (value < seaLevel) {
            value
        } else {
            Float.POSITIVE_INFINITY
        }
    }
    
    do {
        var changed = false
        
        elevation = elevation.mapValues { (tile, value) ->
            var newValue = value
            val lowestAdjacent = tile.adjacent.minBy { elevation[it]!! }!!
            val minElevation = elevation[lowestAdjacent]!!
            if (newValue > elevations[tile]!!) {
                newValue = elevations[tile]!!
            }
            if (newValue < minElevation) {
                newValue = minElevation + epsilon
            }
            if (newValue != value) {
                changed = true
            }
            newValue
        }
    } while (changed)
    
    return elevation
}

fun distributeElevations(elevations: Map<MapGenTile, Float>, random: Random, range: Float, scale: Float): Map<MapGenTile, Float> {
    val temp = Vector3f()
    val seed = Float.fromBits(random.nextInt()) //random float
    return elevations.mapValues { (tile, elevation) ->
        tile.getCenter(temp)
        elevation + range * SimplexNoise.noise(temp.x * scale, temp.y * scale, temp.z * scale, seed)
        
    }
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
            val height = elevations[tile]!!
        
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

fun MapGenerationSettings.generateBiomes(map: Array<MapGenTile>, elevations: Map<MapGenTile, Float>, temperatures: Map<MapGenTile,  Float>, moisture: Map<MapGenTile, Float>): Map<MapGenTile, Biome> {
    val biomes = mutableMapOf<MapGenTile, Biome>()
    linAlgPool.vec3 { vec ->
        map.forEach {
            val h = elevations[it]!!
            val t = temperatures[it]!!
            val m = moisture[it]!!
        
            it.getCenter(vec)
            val n = biomeNoise(vec)
            //        println(n)
            val smallerCount = it.adjacent.count { adj -> elevations[adj]!! <= h }.toDouble() / it.adjacent.size
            val isCoastal = it.adjacent.any { adj -> elevations[adj]!! >= 0 }
            val isCoastal_2 = it.adjacent.any { adj -> adj.adjacent.any { adj2 -> elevations[adj2]!! >= 0 } }
        
            biomes[it] = if (h < 0) {
                when {
                    t < 0.25                                                         -> Biome.ICE
                    h > (-0.015 + n / 15) || isCoastal || (isCoastal_2 && n > -0.15) -> Biome.COAST
                    //                h < -0.025 -> Biome.OCEAN
                    else                                                             -> Biome.OCEAN
                }
            } else if (smallerCount + n / 2 + h < 1.4 / 4) {
                Biome.MOUNTAIN
            } else if (0.85 * t < m && t > 0.5) {
                Biome.RAINFOREST
            } else if (0.45 * t < m && t > 0.4) {
                Biome.FOREST
            } else if (0.35 * t < m) {
                if (t > 0.5) {
                    Biome.DESERT
                } else {
                    Biome.TUNDRA
                }
            } else {
                if (t > 0.4) {
                    Biome.SAVANNA
                } else {
                    Biome.TAIGA
                }
            }
        }
    }
    return biomes
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
    
    set.asSequence().map(Vertex::center).forEach { vertex -> vertex.normalize(len) }
    
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
                MapGenTile(vertex.center, arrayOf(Vector3f(a.center).add(b.center).add(
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
                MapGenTile(vertex.center, arrayOf(Vector3f(a.center).add(b.center).add(
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
        val pathBottom = v1.majorPaths.first { it -> it.last() == v3 }
        val pathTop = v2.majorPaths.first { it -> it.last() == v3 }
        var previous = v1.majorPaths.first { it -> it.last() == v2 }
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

