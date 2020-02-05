package com.sergeysav.hexasphere.client.gl

import com.sergeysav.hexasphere.client.assimp.AMesh
import com.sergeysav.hexasphere.client.bound
import org.joml.Matrix4fc
import org.joml.Vector4fc
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL31
import org.lwjgl.opengl.GL33

/**
 * @author sergeys
 *
 * @constructor Creates a new InstancedMesh
 */
class TileInstancedMeshRenderer(private val mesh: AMesh, private val startingAttributeIndex: Int) {
    
    private val matrixVBO = VertexBufferObject()
    private val colorVBO = VertexBufferObject()
    private var instances: Int = 0
    private var matrixData = floatArrayOf()
    private var colorData = floatArrayOf()
    
    fun setData(instances: Int, matrixGetter: (Int)->Matrix4fc, colorGetter: (Int)->Vector4fc) {
        this.instances = instances
        mesh.vao.bound {
            colorVBO.bind()
            colorData = FloatArray(instances * 4 * 1)
            for (i in 0 until instances) {
                val color = colorGetter(i)
                colorData[i * 4 * 1 + 0] = color.x()
                colorData[i * 4 * 1 + 1] = color.y()
                colorData[i * 4 * 1 + 2] = color.z()
                colorData[i * 4 * 1 + 3] = color.w()
            }
            colorVBO.setData(colorData, GLDataUsage.STATIC)
            
            GL30.glEnableVertexAttribArray(startingAttributeIndex)
            GL30.glVertexAttribPointer(startingAttributeIndex, 4, GL11.GL_FLOAT, false, 1 * 4 * 4, 0 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex, 1)
            
            matrixVBO.bind()
            matrixData = FloatArray(instances * 4 * 4)
            for (i in 0 until instances) {
                matrixGetter(i).get(matrixData, i * 4 * 4)
            }
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
    
    fun updateColors(colorGetter: (Int) -> Vector4fc) {
        mesh.vao.bound {
            colorVBO.bind()
            for (i in 0 until instances) {
                val color = colorGetter(i)
                colorData[i * 4 * 1 + 0] = color.x()
                colorData[i * 4 * 1 + 1] = color.y()
                colorData[i * 4 * 1 + 2] = color.z()
                colorData[i * 4 * 1 + 3] = color.w()
            }
            colorVBO.setData(colorData, GLDataUsage.STATIC)
        }
    }
    
    fun draw(shaderProgram: ShaderProgram) {
        mesh.draw(shaderProgram, false)
        mesh.vao.bound {
            GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, mesh.indices, GL11.GL_UNSIGNED_INT, 0, instances)
        }
    }
    
    fun cleanup() {
        matrixVBO.cleanup()
        colorVBO.cleanup()
        instances = 0
        matrixData = floatArrayOf()
        colorData = floatArrayOf()
    }
}