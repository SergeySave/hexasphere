package com.sergeysav.hexasphere.client.gl

import com.sergeysav.hexasphere.client.bound
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

/**
 * @author sergeys
 *
 * @constructor Creates a new Mesh
 */
class Mesh(private var glDrawingMode: GLDrawingMode, private val useIBOs: Boolean = false) {
    
    private val vao = VertexArrayObject(GL30.glGenVertexArrays())
    private val vbo = VertexBufferObject(GL15.glGenBuffers())
    private val ibo = ElementBufferObject(if (useIBOs) GL15.glGenBuffers() else 0) // 0 = null
    private var vertexCount = 0
    private var indexCount = 0
    private lateinit var vertexGLDataUsage: GLDataUsage
    
    fun setVertices(data: FloatArray, dataUsage: GLDataUsage, vararg attributes: VertexAttribute) {
        val stride = attributes.map(VertexAttribute::totalLength).sum()
        vertexCount = data.size/attributes.map(VertexAttribute::components).sum()
        vertexGLDataUsage = dataUsage
        
        vao.bound {
            vbo.bind()
            vbo.setData(data, dataUsage)
            
            var offset = 0L
            for ((index, attribute) in attributes.withIndex()) {
                GL20.glVertexAttribPointer(index, attribute.components, attribute.type, attribute.normalized, stride, offset)
                GL20.glEnableVertexAttribArray(index)
                offset += attribute.totalLength
            }
        }
    }
    
    fun setVertexData(data: FloatArray) {
        vao.bound {
            vbo.bind()
            vbo.setData(data, vertexGLDataUsage)
        }
    }
    
    fun setIndexData(data: IntArray, dataUsage: GLDataUsage) {
        if (!useIBOs) {
            throw IllegalStateException("Mesh Index data disabled")
        }
        vao.bound {
            ibo.bind()
            ibo.setData(data, dataUsage)
        }
        indexCount = data.size
    }
    
    fun draw(numIndices: Int = indexCount) {
        bound {
            if (useIBOs) {
                GL11.glDrawElements(glDrawingMode.mode, numIndices, GL11.GL_UNSIGNED_INT, 0)
            } else {
                GL11.glDrawArrays(glDrawingMode.mode, 0, vertexCount)
            }
        }
    }
    
    fun bound(inner: ()->Unit) {
        vao.bound {
            vbo.bind()
            ibo.bind()
            inner()
        }
    }
    
    fun cleanup() {
        if (useIBOs) {
            ibo.cleanup()
        }
        vbo.cleanup()
        vao.cleanup()
    }
}
