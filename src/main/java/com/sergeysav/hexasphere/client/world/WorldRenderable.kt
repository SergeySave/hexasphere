package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.gl.GLDataUsage
import com.sergeysav.hexasphere.client.gl.Mesh
import com.sergeysav.hexasphere.client.gl.Texture2D
import com.sergeysav.hexasphere.client.gl.Vec2VertexAttribute
import com.sergeysav.hexasphere.client.gl.Vec3VertexAttribute
import com.sergeysav.hexasphere.common.world.World
import com.sergeysav.hexasphere.common.world.tile.Tile
import org.joml.Matrix4f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author sergeys
 *
 * @constructor Creates a new WorldRenderable
 */
data class WorldRenderable(val world: World, val modelMatrix: Matrix4f,
                           val mesh: Mesh,
                           val texture: Texture2D) {
    private val floatsPerVert = 3 + 3 + 2
    private val vertices = FloatArray(floatsPerVert * world.numVertices)
    private val indices = IntArray(3 * world.numTriangles)
    
    fun prepareMesh(inner: ()->Unit) {
        world.apply {
            for (i in 0 until numPentagons) {
                val verts = tiles[i].tilePolygon.vertices
//                val biome = tiles[i].biome
                val (r, g, b) = tiles[i].getColoring()
                val (u, v) = tiles[i].getImageCoords()
                for (j in 0 until verts.size) {
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 0] = verts[j].x()
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 1] = verts[j].y()
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 2] = verts[j].z()
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 3] = r
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 4] = g
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 5] = b
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 6] = (0.25f + 0.25f * cos(j * 2f * PI / verts.size + PI/2).toFloat() + u / 2f)
                    vertices[5 * floatsPerVert * i + floatsPerVert * j + 7] = (0.25f + 0.25f * sin(j * 2f * PI / verts.size + PI/2).toFloat() + v / 2f)
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
//                val biome = tiles[i + numPentagons].biome
                val (r, g, b) = tiles[i + numPentagons].getColoring()
                val (u, v) = tiles[i + numPentagons].getImageCoords()
                for (j in 0 until verts.size) {
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 0 + floatsPerVert * hexVOffset] = verts[j].x()
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 1 + floatsPerVert * hexVOffset] = verts[j].y()
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 2 + floatsPerVert * hexVOffset] = verts[j].z()
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 3 + floatsPerVert * hexVOffset] = r
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 4 + floatsPerVert * hexVOffset] = g
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 5 + floatsPerVert * hexVOffset] = b
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 6 + floatsPerVert * hexVOffset] = (0.25f + 0.25f * cos(j * 2f * PI / verts.size + PI/2).toFloat() + u / 2f)
                    vertices[6 * floatsPerVert * i + floatsPerVert * j + 7 + floatsPerVert * hexVOffset] = (0.25f + 0.25f * sin(j * 2f * PI / verts.size + PI/2).toFloat() + v / 2f)
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
                         Vec3VertexAttribute("aColor"),
                         Vec2VertexAttribute("aUV"))
    
        mesh.bound(inner)
    
        mesh.setIndexData(indices, GLDataUsage.STATIC)
    }
    
    fun updateMesh(mouseoverTile: Tile?) {
        world.apply {
            for (i in 0 until numPentagons) {
//                val biome = tiles[i].biome
                val (r, g, b) = tiles[i].getColoring()
                for (j in 0 until tiles[i].tilePolygon.polygonType.vertices) {
                    if (mouseoverTile == tiles[i]) {
                        vertices[5 * floatsPerVert * i + floatsPerVert * j + 3] = 1.0f
                        vertices[5 * floatsPerVert * i + floatsPerVert * j + 4] = 0.0f
                        vertices[5 * floatsPerVert * i + floatsPerVert * j + 5] = 0.0f
                    } else {
                        vertices[5 * floatsPerVert * i + floatsPerVert * j + 3] = r
                        vertices[5 * floatsPerVert * i + floatsPerVert * j + 4] = g
                        vertices[5 * floatsPerVert * i + floatsPerVert * j + 5] = b
                    }
                }
            }
            val hexVOffset = numPentagons * 5
            for (i in 0 until numHexagons) {
//                val biome = tiles[i + numPentagons].biome
                val (r, g, b) = tiles[i + numPentagons].getColoring()
                for (j in 0 until tiles[i + numPentagons].tilePolygon.polygonType.vertices) {
                    if (mouseoverTile == tiles[i + numPentagons]) {
                        vertices[6 * floatsPerVert * i + floatsPerVert * j + 3 + floatsPerVert * hexVOffset] = 1.0f
                        vertices[6 * floatsPerVert * i + floatsPerVert * j + 4 + floatsPerVert * hexVOffset] = 0.0f
                        vertices[6 * floatsPerVert * i + floatsPerVert * j + 5 + floatsPerVert * hexVOffset] = 0.0f
                    } else {
                        vertices[6 * floatsPerVert * i + floatsPerVert * j + 3 + floatsPerVert * hexVOffset] = r
                        vertices[6 * floatsPerVert * i + floatsPerVert * j + 4 + floatsPerVert * hexVOffset] = g
                        vertices[6 * floatsPerVert * i + floatsPerVert * j + 5 + floatsPerVert * hexVOffset] = b
                    }
                }
            }
        }
        mesh.setVertexData(vertices)
    }
}