package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.gl.Vec2VertexAttribute
import com.sergeysav.hexasphere.client.gl.Vec3VertexAttribute
import com.sergeysav.hexasphere.common.world.tile.Tile
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author sergeys
 */
object TileBaker {
    const val FLOATS_PER_VERT = 3 + 3 + 2
    val vertexAttributes = arrayOf(Vec3VertexAttribute("aPos"),
                                   Vec3VertexAttribute("aColor"),
                                   Vec2VertexAttribute("aUV"))
    
    fun bakeTileVertices(tile: Tile, vertexArray: FloatArray, offset: Int) {
        val (r, g, b) = tile.getColoring()
        val (u, v) = tile.getImageCoords()
        
        val numVerts = tile.tilePolygon.polygonType.vertices
        for (j in 0 until numVerts) {
            vertexArray[FLOATS_PER_VERT * j + 0 + offset] = tile.tilePolygon.vertices[j].x()
            vertexArray[FLOATS_PER_VERT * j + 1 + offset] = tile.tilePolygon.vertices[j].y()
            vertexArray[FLOATS_PER_VERT * j + 2 + offset] = tile.tilePolygon.vertices[j].z()
            vertexArray[FLOATS_PER_VERT * j + 3 + offset] = r
            vertexArray[FLOATS_PER_VERT * j + 4 + offset] = g
            vertexArray[FLOATS_PER_VERT * j + 5 + offset] = b
            vertexArray[FLOATS_PER_VERT * j + 6 + offset] = (0.25f + 0.25f * cos(
                    j * 2f * PI / numVerts + PI / 2).toFloat() + u / 2f)
            vertexArray[FLOATS_PER_VERT * j + 7 + offset] = (0.25f + 0.25f * sin(
                    j * 2f * PI / numVerts + PI / 2).toFloat() + v / 2f)
        }
    }
    
    fun bakeTileIndices(numVertices: Int, indexArray: IntArray, offset: Int, vertexOffset: Int) {
        for (j in 2 until numVertices) {
            indexArray[3 * (j - 2) + 0 + offset] = vertexOffset
            indexArray[3 * (j - 2) + 1 + offset] = vertexOffset + j - 1
            indexArray[3 * (j - 2) + 2 + offset] = vertexOffset + j
        }
    }
    
    fun updateTileVertices(tile: Tile, isMouseover: Boolean, vertexArray: FloatArray, offset: Int) {
        val (r, g, b) = tile.getColoring()
        for (j in 0 until tile.tilePolygon.polygonType.vertices) {
            if (isMouseover) {
                vertexArray[offset + FLOATS_PER_VERT * j + 3] = 1.0f
                vertexArray[offset + FLOATS_PER_VERT * j + 4] = 0.0f
                vertexArray[offset + FLOATS_PER_VERT * j + 5] = 0.0f
            } else {
                vertexArray[offset + FLOATS_PER_VERT * j + 3] = r
                vertexArray[offset + FLOATS_PER_VERT * j + 4] = g
                vertexArray[offset + FLOATS_PER_VERT * j + 5] = b
            }
        }
    }
}