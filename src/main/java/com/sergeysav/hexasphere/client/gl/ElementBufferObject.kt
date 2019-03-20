package com.sergeysav.hexasphere.client.gl

import com.sergeysav.hexasphere.client.Bindable
import org.lwjgl.opengl.GL15

inline class ElementBufferObject(val id: Int): Bindable {
    fun cleanup() = GL15.glDeleteBuffers(id)
    override fun bind() = GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id)
    override fun unbind() = GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
    fun setData(data: IntArray, usage: GLDataUsage) =
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, usage.draw)
    
    //    fun setData(data: IntBuffer, usage: GLDataUsage) =
    //            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, usage.draw)
}
