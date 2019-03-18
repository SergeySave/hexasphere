package com.sergeysav.hexasphere.gl

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

/**
 * @author sergeys
 */
inline class Texture2D(val id: Int) {
    companion object {
        fun new() = Texture2D(GL11.glGenTextures())
    }
}
fun Texture2D.cleanup() = GL11.glDeleteTextures(id)
fun Texture2D.bind() {
    GL20.glActiveTexture(GL20.GL_TEXTURE0)
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
}
@Suppress("unused") //Keep it a method on VAO so that it's clear what it's unbinding
fun Texture2D.unbind() = GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
fun Texture2D.bound(inner: ()->Unit) {
    try {
        bind()
        inner()
    } finally {
        unbind()
    }
}

enum class TextureWrapMode(val id: Int) {
    REPEAT(GL11.GL_REPEAT),
    MIRRORED_REPEAT(GL20.GL_MIRRORED_REPEAT),
    CLAMP_TO_EDGE(GL20.GL_CLAMP_TO_EDGE),
    CLAMP_TO_COLOR(GL20.GL_CLAMP_TO_BORDER),
}

enum class TextureInterpolationMode(val id: Int) {
    LINEAR(GL11.GL_LINEAR),
    NEAREST(GL11.GL_NEAREST),
    BI_MIPMAP_LINEAR(GL11.GL_LINEAR_MIPMAP_LINEAR),
    NEAR_MIPMAP_LINEAR(GL11.GL_NEAREST_MIPMAP_LINEAR),
    BI_MIPMAP_NEAREST(GL11.GL_LINEAR_MIPMAP_NEAREST),
    NEAR_MIPMAP_NEAREST(GL11.GL_NEAREST_MIPMAP_NEAREST)
}
