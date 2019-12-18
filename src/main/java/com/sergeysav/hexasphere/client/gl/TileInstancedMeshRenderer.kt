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
    
    private val vbo = VertexBufferObject()
    private var instances: Int = 0
    private var data = FloatArray(0)
    
    fun setData(instances: Int, matrixGetter: (Int)->Matrix4fc, colorGetter: (Int)->Vector4fc) {
        this.instances = instances
        mesh.vao.bound {
            vbo.bind()
            data = FloatArray(instances * 4 * 5)
            for (i in 0 until instances) {
                val color = colorGetter(i)
                data[i * 4 * 5 + 0] = color.x()
                data[i * 4 * 5 + 1] = color.y()
                data[i * 4 * 5 + 2] = color.z()
                data[i * 4 * 5 + 3] = color.w()
                matrixGetter(i).get(data, i * 4 * 5 + 4)
            }
            vbo.setData(data, GLDataUsage.STATIC)
    
            GL30.glEnableVertexAttribArray(startingAttributeIndex)
            GL30.glVertexAttribPointer(startingAttributeIndex, 4, GL11.GL_FLOAT, false, 5 * 4 * 4, 0 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex, 1)

            GL30.glEnableVertexAttribArray(startingAttributeIndex + 1)
            GL30.glVertexAttribPointer(startingAttributeIndex + 1, 4, GL11.GL_FLOAT, false, 5 * 4 * 4, 1 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 1, 1)
        
            GL30.glEnableVertexAttribArray(startingAttributeIndex + 2)
            GL30.glVertexAttribPointer(startingAttributeIndex + 2, 4, GL11.GL_FLOAT, false, 5 * 4 * 4, 2 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 2, 1)
        
            GL30.glEnableVertexAttribArray(startingAttributeIndex + 3)
            GL30.glVertexAttribPointer(startingAttributeIndex + 3, 4, GL11.GL_FLOAT, false, 5 * 4 * 4, 3 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 3, 1)
        
            GL30.glEnableVertexAttribArray(startingAttributeIndex + 4)
            GL30.glVertexAttribPointer(startingAttributeIndex + 4, 4, GL11.GL_FLOAT, false, 5 * 4 * 4, 4 * 4 * 4)
            GL33.glVertexAttribDivisor(startingAttributeIndex + 4, 1)
        }
    }
    
    fun updateColors(colorGetter: (Int) -> Vector4fc) {
        mesh.vao.bound {
            vbo.bind()
            for (i in 0 until instances) {
                val color = colorGetter(i)
                data[i * 4 * 5 + 0] = color.x()
                data[i * 4 * 5 + 1] = color.y()
                data[i * 4 * 5 + 2] = color.z()
                data[i * 4 * 5 + 3] = color.w()
            }
            vbo.setData(data, GLDataUsage.STATIC)
        }
    }
    
    fun draw(shaderProgram: ShaderProgram) {
        mesh.draw(shaderProgram, false)
        mesh.vao.bound {
            GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, mesh.indices, GL11.GL_UNSIGNED_INT, 0, instances)
        }
    }
    
    fun cleanup() {
        vbo.cleanup()
    }
}