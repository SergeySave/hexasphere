package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.assimp.AssimpUtils
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.world.World
import com.sergeysav.hexasphere.common.world.tile.Tile
import com.sergeysav.hexasphere.common.world.tile.TileWedge
import org.joml.Matrix4f

/**
 * @author sergeys
 *
 * @constructor Creates a new WorldRenderable
 */
class WorldRenderable(val world: World, val modelMatrix: Matrix4f, private val linAlgPool: LinAlgPool) {
    private var lastMouseoverTile: Tile? = null
    
    private val renderers = mutableMapOf<String, WedgeInstancedMeshRenderer>()
    private val wedgeResources = mutableMapOf<TileWedge, String>()

    fun prepareMesh(inner: ()->Unit) {
        world.tiles.forEach { tile ->
            tile.tilePolygon.wedges.forEachIndexed { i, wedge ->
                val resource = tile.type.getWedgeResource(tile.adjacent[i].type)
                wedgeResources[wedge] = resource
                if (resource !in renderers) {
                    renderers[resource] = WedgeInstancedMeshRenderer(AssimpUtils.loadTileMesh(resource), 5, linAlgPool)
                }
                renderers[resource]!!.addWedge(wedge)
            }
        }
    }

    fun updateMesh(mouseoverTile: Tile?) {
        val lastMouseoverTile = lastMouseoverTile
        if (lastMouseoverTile != null) {
            linAlgPool.vec4 { vec ->
                vec.set(1f, 1f, 1f, 1f)
                lastMouseoverTile.tilePolygon.wedges.forEach { wedge ->
                    renderers[wedgeResources[wedge]]?.updateWedge(wedge, vec)
                }
            }
        }
        this.lastMouseoverTile = mouseoverTile
        if (mouseoverTile != null) {
            linAlgPool.vec4 { vec ->
                vec.set(1f, 0f, 0f, 1f)
                mouseoverTile.tilePolygon.wedges.forEach { wedge ->
                    renderers[wedgeResources[wedge]]?.updateWedge(wedge, vec)
                }
            }
        }
    }
    
    fun draw(shaderProgram: ShaderProgram) {
        renderers.values.forEach { it.draw(shaderProgram) }
    }

    fun cleanup() {
        renderers.values.forEach(WedgeInstancedMeshRenderer::cleanup)
    }
}