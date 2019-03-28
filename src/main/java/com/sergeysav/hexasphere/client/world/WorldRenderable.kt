package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.gl.GLDataUsage
import com.sergeysav.hexasphere.client.gl.Mesh
import com.sergeysav.hexasphere.client.gl.Texture2D
import com.sergeysav.hexasphere.common.world.World
import com.sergeysav.hexasphere.common.world.tile.Tile
import org.joml.Matrix4f

/**
 * @author sergeys
 *
 * @constructor Creates a new WorldRenderable
 */
data class WorldRenderable(val world: World, val modelMatrix: Matrix4f,
                           val mesh: Mesh, val texture: Texture2D) {
    private val vertices = FloatArray(TileBaker.FLOATS_PER_VERT * world.numVertices)
    private val indices = IntArray(3 * world.numTriangles)
    
    fun prepareMesh(inner: ()->Unit) {
        world.apply {
            for (i in 0 until numPentagons) {
                TileBaker.bakeTileVertices(tiles[i], vertices, 5 * TileBaker.FLOATS_PER_VERT * i)
                TileBaker.bakeTileIndices(5, indices, (5 - 2) * 3 * i, 5 * i)
            }
            val hexVOffset = numPentagons * 5
            val hexIOffset = (5 - 2) * 3 * numPentagons
            for (i in 0 until numHexagons) {
                TileBaker.bakeTileVertices(tiles[i + numPentagons], vertices,
                                           6 * TileBaker.FLOATS_PER_VERT * i + TileBaker.FLOATS_PER_VERT * hexVOffset)
                TileBaker.bakeTileIndices(6, indices, (6 - 2) * 3 * i + hexIOffset, 6 * i + hexVOffset)
            }
        }
    
        mesh.setVertices(vertices, GLDataUsage.DYNAMIC, *TileBaker.vertexAttributes)
        mesh.bound(inner)
        mesh.setIndexData(indices, GLDataUsage.STATIC)
    }
    
    fun updateMesh(mouseoverTile: Tile?) {
        world.apply {
            for (i in 0 until numPentagons) {
                TileBaker.updateTileVertices(tiles[i], mouseoverTile == tiles[i], vertices,
                                             5 * TileBaker.FLOATS_PER_VERT * i)
            }
            val hexVOffset = numPentagons * 5
            for (i in 0 until numHexagons) {
                TileBaker.updateTileVertices(tiles[i + numPentagons], mouseoverTile == tiles[i + numPentagons],
                                             vertices,
                                             6 * TileBaker.FLOATS_PER_VERT * i + TileBaker.FLOATS_PER_VERT * hexVOffset)
            }
        }
        mesh.setVertexData(vertices)
    }
}