package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.assimp.AMesh
import com.sergeysav.hexasphere.client.bound
import com.sergeysav.hexasphere.client.gl.GLDataUsage
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.client.gl.VertexBufferObject
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.data.MutableBiMap
import com.sergeysav.hexasphere.common.world.tile.TileWedge
import org.joml.Vector4fc
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL31
import org.lwjgl.opengl.GL33
import kotlin.math.roundToInt

/**
 * @author sergeys
 *
 * @constructor Creates a new InstancedMesh
 */
class WedgeInstancedMeshRenderer(private val mesh: AMesh, private val startingAttributeIndex: Int, private val linAlgPool: LinAlgPool) {
    
    private val scaleFactor = 1.5
    private val radialScale = 0.02f
    private val matrixVBO = VertexBufferObject()
    private val colorVBO = VertexBufferObject()
    private val wedgeIndex = MutableBiMap<TileWedge, Int>()
    private var instances: Int = 0
    private var matrixDirty: Boolean = true
    private var matrixData = FloatArray(16 * 50)
    private var colorDirty: Boolean = true
    private var colorData = FloatArray(4 * 50)
    
    init {
        mesh.vao.bound {
            colorVBO.bind()
            colorVBO.setData(colorData, GLDataUsage.STATIC)
            GL30.glEnableVertexAttribArray(startingAttributeIndex)
            GL30.glVertexAttribPointer(startingAttributeIndex, 4, GL11.GL_FLOAT, false, 1 * 4 * 4, 0 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex, 1)
            
            matrixVBO.bind()
            matrixVBO.setData(matrixData, GLDataUsage.STATIC)
            GL30.glEnableVertexAttribArray(startingAttributeIndex + 1)
            GL30.glVertexAttribPointer(startingAttributeIndex + 1, 4, GL11.GL_FLOAT, false, 4 * 4 * 4, 0 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 1, 1)
    
            GL30.glEnableVertexAttribArray(startingAttributeIndex + 2)
            GL30.glVertexAttribPointer(startingAttributeIndex + 2, 4, GL11.GL_FLOAT, false, 4 * 4 * 4, 1 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 2, 1)
    
            GL30.glEnableVertexAttribArray(startingAttributeIndex + 3)
            GL30.glVertexAttribPointer(startingAttributeIndex + 3, 4, GL11.GL_FLOAT, false, 4 * 4 * 4, 2 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 3, 1)
    
            GL30.glEnableVertexAttribArray(startingAttributeIndex + 4)
            GL30.glVertexAttribPointer(startingAttributeIndex + 4, 4, GL11.GL_FLOAT, false, 4 * 4 * 4, 3 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 4, 1)
        }
    }
    
    fun addWedge(wedge: TileWedge) {
        val existingIndex = wedgeIndex.getForward(wedge)
        if (existingIndex == null) {
            val index = instances++
            wedgeIndex.put(wedge, index)
            while (matrixData.size < (index + 1) * 16) {
                matrixData = matrixData.copyOf((matrixData.size * scaleFactor).roundToInt())
            }
            while (colorData.size < (index + 1) * 4) {
                colorData = colorData.copyOf((colorData.size * scaleFactor).roundToInt())
            }
            linAlgPool.mat4 {
                wedge.setMatrix(it, radialScale)
                it.get(matrixData, index * 16)
                matrixDirty = true
            }
            colorData[index * 4 + 0] = 1f
            colorData[index * 4 + 1] = 1f
            colorData[index * 4 + 2] = 1f
            colorData[index * 4 + 3] = 1f
            colorDirty = true
        } else {
            linAlgPool.mat4 {
                wedge.setMatrix(it, radialScale)
                it.get(matrixData, existingIndex * 16)
                matrixDirty = true
            }
            colorData[existingIndex * 4 + 0] = 1f
            colorData[existingIndex * 4 + 1] = 1f
            colorData[existingIndex * 4 + 2] = 1f
            colorData[existingIndex * 4 + 3] = 1f
            colorDirty = true
        }
    }
    
//    fun addTile(tile: Tile) {
////        if (tile.type == TerrainType.PlainsRiver) {
////            addWedge(tile.tilePolygon.wedges[0])
////        } else {
//            tile.tilePolygon.wedges.forEach(this::addWedge)
////        }
//    }
    
    fun removeWedge(wedge: TileWedge) {
        val indexToRemove = wedgeIndex.removeForward(wedge) ?: return
        val otherIndex = instances--
        wedgeIndex.put(wedgeIndex.removeBackward(otherIndex)!!, indexToRemove)
        
        // Remove matrix data
        System.arraycopy(matrixData, otherIndex * 16, matrixData, indexToRemove * 16, 16)
        matrixDirty = true
        System.arraycopy(colorData, otherIndex * 4, colorData, indexToRemove * 4, 4)
        colorDirty = true
    }
    
//    fun removeTile(tile: Tile) {
//        tile.tilePolygon.wedges.forEach(this::removeWedge)
//    }
    
    fun updateWedge(wedge: TileWedge, color: Vector4fc) {
        val index = wedgeIndex.getForward(wedge) ?: return
    
        colorData[index * 4 + 0] = color.x()
        colorData[index * 4 + 1] = color.y()
        colorData[index * 4 + 2] = color.z()
        colorData[index * 4 + 3] = color.w()
        colorDirty = true
    }
    
//    fun updateTile(tile: Tile, color: Vector4fc) {
//        tile.tilePolygon.wedges.forEach { updateWedge(it, color) }
//    }
    
    fun draw(shaderProgram: ShaderProgram) {
        mesh.draw(shaderProgram, false)
        mesh.vao.bound {
            if (colorDirty) {
                colorVBO.bind()
                colorVBO.setData(colorData, GLDataUsage.STATIC)
                colorDirty = false
            }
            if (matrixDirty) {
                matrixVBO.bind()
                matrixVBO.setData(matrixData, GLDataUsage.STATIC)
                matrixDirty = false
            }
            GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, mesh.indices, GL11.GL_UNSIGNED_INT, 0, instances)
        }
    }
    
    fun cleanup() {
        matrixVBO.cleanup()
        colorVBO.cleanup()
        instances = 0
        matrixData = floatArrayOf()
        colorData = floatArrayOf()
        wedgeIndex.clear()
    }
}