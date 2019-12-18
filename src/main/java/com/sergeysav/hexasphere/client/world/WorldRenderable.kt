package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.assimp.AMesh
import com.sergeysav.hexasphere.client.gl.GLDataUsage
import com.sergeysav.hexasphere.client.gl.Mesh
import com.sergeysav.hexasphere.client.gl.Texture2D
import com.sergeysav.hexasphere.client.gl.TileInstancedMeshRenderer
import com.sergeysav.hexasphere.common.world.World
import com.sergeysav.hexasphere.common.world.tile.Tile
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainShape
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * @author sergeys
 *
 * @constructor Creates a new WorldRenderable
 */
class WorldRenderable(val world: World, val modelMatrix: Matrix4f,
                           val mesh: Mesh, val texture: Texture2D, flatMesh: AMesh) {
    private val vertices = FloatArray(TileBaker.FLOATS_PER_VERT * world.numVertices)
    private val indices = IntArray(3 * world.numTriangles)
    val flatInstanceRenderer = TileInstancedMeshRenderer(flatMesh, 3)
    val matrices = mutableListOf<Matrix4f>()
    val flatTileIndices = mutableListOf<Int>()
    
    fun prepareMesh(inner: ()->Unit) {
        world.apply {
            for (i in 0 until numPentagons) {
                TileBaker.bakeTileVertices(tiles[i], vertices, 5 * TileBaker.FLOATS_PER_VERT * i)
                if (tiles[i].shape == TerrainShape.FlatTerrainShape) {
                    for (j in 2 until 5) {
                        indices[3 * (j - 2) + 0 + (5 - 2) * 3 * i] = 0
                        indices[3 * (j - 2) + 1 + (5 - 2) * 3 * i] = 0
                        indices[3 * (j - 2) + 2 + (5 - 2) * 3 * i] = 0
                    }
                } else {
                    TileBaker.bakeTileIndices(5, indices, (5 - 2) * 3 * i, 5 * i)
                }
            }
            val hexVOffset = numPentagons * 5
            val hexIOffset = (5 - 2) * 3 * numPentagons
            for (i in 0 until numHexagons) {
                TileBaker.bakeTileVertices(tiles[i + numPentagons], vertices,
                                           6 * TileBaker.FLOATS_PER_VERT * i + TileBaker.FLOATS_PER_VERT * hexVOffset)
                if (tiles[i + numPentagons].shape == TerrainShape.FlatTerrainShape) {
                    for (j in 2 until 6) {
                        indices[3 * (j - 2) + 0 + (6 - 2) * 3 * i + hexIOffset] = 0
                        indices[3 * (j - 2) + 1 + (6 - 2) * 3 * i + hexIOffset] = 0
                        indices[3 * (j - 2) + 2 + (6 - 2) * 3 * i + hexIOffset] = 0
                    }
                } else {
                    TileBaker.bakeTileIndices(6, indices, (6 - 2) * 3 * i + hexIOffset, 6 * i + hexVOffset)
                }
            }
        }
    
        mesh.setVertices(vertices, GLDataUsage.DYNAMIC, *TileBaker.vertexAttributes)
        mesh.bound(inner)
        mesh.setIndexData(indices, GLDataUsage.STATIC)
    
        val vertex1 = Vector3f()
        val vertex2 = Vector3f()
        val center = Vector3f()
        val radialScale = 0.25f
        world.tiles.forEachIndexed { j, tile ->
            if (tile.shape == TerrainShape.FlatTerrainShape) {
                for (i in tile.tilePolygon.vertices.indices) {
                    vertex2.set(tile.tilePolygon.vertices[i])
                    vertex1.set(tile.tilePolygon.vertices[(i + 1) % tile.tilePolygon.vertices.size])
                    center.set(tile.tilePolygon.center)
                    val matrix4f = Matrix4f()
                    matrix4f.set(vertex1.x() - center.x(), vertex1.y() - center.y(), vertex1.z() - center.z(), 0f,
                                 center.x() * radialScale, center.y() * radialScale, center.z() * radialScale, 0f,
                                 vertex2.x() - center.x(), vertex2.y() - center.y(), vertex2.z() - center.z(), 0f,
                                 center.x(), center.y(), center.z(), 1f)
                    matrix4f.mulLocal(modelMatrix)
                    matrices.add(matrix4f)
                    flatTileIndices.add(j)
                }
            }
        }
    
        flatInstanceRenderer.setData(matrices.size, { matrices[it] }, { Vector4f(1f, 1f, 1f, 1f) })
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
    
        val colorVector = Vector4f()
        flatInstanceRenderer.setData(matrices.size, { matrices[it] }, {
            val tile = world.tiles[flatTileIndices[it]]
            if (tile == mouseoverTile) {
                colorVector.set(1f, 0f, 0f, 1f)
            } else {
                colorVector.set(1f, 1f, 1f, 1f)
            }
        })
    }
    
    fun cleanup() {
        flatInstanceRenderer.cleanup()
    }
}