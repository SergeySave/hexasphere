package com.sergeysav.hexasphere.gl

import org.lwjgl.opengl.GL15
import java.nio.FloatBuffer

inline class VertexBufferObject(val id: Int)
fun VertexBufferObject.cleanup() = GL15.glDeleteBuffers(id)
fun VertexBufferObject.bind() = GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id)
@Suppress("unused") //Keep it a method on VBO so that it's clear what it's unbinding
fun VertexBufferObject.unbind() = GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
fun VertexBufferObject.setData(data: FloatArray, usage: GLDataUsage) = GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, usage.draw)
fun VertexBufferObject.setData(data: FloatBuffer, usage: GLDataUsage) = GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, usage.draw)
