package com.sergeysav.hexasphere.map

import com.sergeysav.hexasphere.gl.GLDataUsage
import com.sergeysav.hexasphere.gl.Mesh
import com.sergeysav.hexasphere.gl.Vec3VertexAttribute
import com.sergeysav.hexasphere.map.tile.Tile
import org.joml.Matrix4f

/**
 * @author sergeys
 *
 * @constructor Creates a new WorldRenderable
 */
data class WorldRenderable(val world: World, val modelMatrix: Matrix4f, val mesh: Mesh) {
    private val vertices = FloatArray(6 * world.numVertices)
    private val indices = IntArray(3 * world.numTriangles)
    
    fun prepareMesh(inner: ()->Unit) {
        world.apply {
            for (i in 0 until numPentagons) {
                val verts = tiles[i].tilePolygon.vertices
                val biome = tiles[i].biome
                for (j in 0 until verts.size) {
                    vertices[5 * 6 * i + 6 * j + 0] = verts[j].x()
                    vertices[5 * 6 * i + 6 * j + 1] = verts[j].y()
                    vertices[5 * 6 * i + 6 * j + 2] = verts[j].z()
                    vertices[5 * 6 * i + 6 * j + 3] = biome.r
                    vertices[5 * 6 * i + 6 * j + 4] = biome.g
                    vertices[5 * 6 * i + 6 * j + 5] = biome.b
                }
                for (j in 2 until verts.size) {
                    indices[(5 - 2) * 3 * i + 3 * (j - 2) + 0] = 5 * i
                    indices[(5 - 2) * 3 * i + 3 * (j - 2) + 1] = 5 * i + j - 1
                    indices[(5 - 2) * 3 * i + 3 * (j - 2) + 2] = 5 * i + j
                }
            }
            val hexVOffset = numPentagons * 5
            val hexIOffset = (5 - 2) * 3 * numPentagons
            for (i in 0 until numHexagons) {
                val verts = tiles[i + numPentagons].tilePolygon.vertices
                val biome = tiles[i + numPentagons].biome
                for (j in 0 until verts.size) {
                    vertices[6 * 6 * i + 6 * j + 0 + 6 * hexVOffset] = verts[j].x()
                    vertices[6 * 6 * i + 6 * j + 1 + 6 * hexVOffset] = verts[j].y()
                    vertices[6 * 6 * i + 6 * j + 2 + 6 * hexVOffset] = verts[j].z()
                    vertices[6 * 6 * i + 6 * j + 3 + 6 * hexVOffset] = biome.r
                    vertices[6 * 6 * i + 6 * j + 4 + 6 * hexVOffset] = biome.g
                    vertices[6 * 6 * i + 6 * j + 5 + 6 * hexVOffset] = biome.b
                }
                for (j in 2 until verts.size) {
                    indices[(6 - 2) * 3 * i + 3 * (j - 2) + 0 + hexIOffset] = 6 * i + hexVOffset
                    indices[(6 - 2) * 3 * i + 3 * (j - 2) + 1 + hexIOffset] = 6 * i + j - 1 + hexVOffset
                    indices[(6 - 2) * 3 * i + 3 * (j - 2) + 2 + hexIOffset] = 6 * i + j + hexVOffset
                }
            }
        }
        
        mesh.setVertices(vertices, GLDataUsage.DYNAMIC,
                         Vec3VertexAttribute("aPos"),
                         Vec3VertexAttribute("aColor"))
    
        mesh.bound(inner)
    
        mesh.setIndexData(indices, GLDataUsage.STATIC)
    }
    
    fun updateMesh(mouseoverTile: Tile?) {
        world.apply {
            for (i in 0 until numPentagons) {
                val biome = tiles[i].biome
                for (j in 0 until tiles[i].tilePolygon.polygonType.vertices) {
                    if (mouseoverTile == tiles[i]) {
                        vertices[5 * 6 * i + 6 * j + 3] = 1.0f
                        vertices[5 * 6 * i + 6 * j + 4] = 0.0f
                        vertices[5 * 6 * i + 6 * j + 5] = 0.0f
                    } else {
                        vertices[5 * 6 * i + 6 * j + 3] = biome.r
                        vertices[5 * 6 * i + 6 * j + 4] = biome.g
                        vertices[5 * 6 * i + 6 * j + 5] = biome.b
                    }
                }
            }
            val hexVOffset = numPentagons * 5
            for (i in 0 until numHexagons) {
                val biome = tiles[i + numPentagons].biome
                for (j in 0 until tiles[i + numPentagons].tilePolygon.polygonType.vertices) {
                    if (mouseoverTile == tiles[i + numPentagons]) {
                        vertices[6 * 6 * i + 6 * j + 3 + 6 * hexVOffset] = 1.0f
                        vertices[6 * 6 * i + 6 * j + 4 + 6 * hexVOffset] = 0.0f
                        vertices[6 * 6 * i + 6 * j + 5 + 6 * hexVOffset] = 0.0f
                    } else {
                        vertices[6 * 6 * i + 6 * j + 3 + 6 * hexVOffset] = biome.r
                        vertices[6 * 6 * i + 6 * j + 4 + 6 * hexVOffset] = biome.g
                        vertices[6 * 6 * i + 6 * j + 5 + 6 * hexVOffset] = biome.b
                    }
                }
            }
        }
        mesh.setVertexData(vertices)
    }
}