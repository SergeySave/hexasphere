package com.sergeysav.hexasphere.gl

import org.lwjgl.opengl.GL15
import java.nio.IntBuffer

inline class ElementBufferObject(val id: Int)
fun ElementBufferObject.cleanup() = GL15.glDeleteBuffers(id)
fun ElementBufferObject.bind() = GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id)
@Suppress("unused") //Keep it a method on EBO so that it's clear what it's unbinding
fun ElementBufferObject.unbind() = GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0)
fun ElementBufferObject.setData(data: IntArray, usage: GLDataUsage) = GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, usage.draw)
fun ElementBufferObject.setData(data: IntBuffer, usage: GLDataUsage) = GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, usage.draw)
