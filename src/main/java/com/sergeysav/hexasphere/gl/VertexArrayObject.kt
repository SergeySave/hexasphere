package com.sergeysav.hexasphere.gl

import org.lwjgl.opengl.GL30

inline class VertexArrayObject(val id: Int)
fun VertexArrayObject.cleanup() = GL30.glDeleteVertexArrays(id)
fun VertexArrayObject.bind() = GL30.glBindVertexArray(id)
@Suppress("unused") //Keep it a method on VAO so that it's clear what it's unbinding
fun VertexArrayObject.unbind() = GL30.glBindVertexArray(0)
fun VertexArrayObject.bound(inner: ()->Unit) {
    try {
        bind()
        inner()
    } finally {
        unbind()
    }
}
