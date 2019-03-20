package com.sergeysav.hexasphere.client.gl

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

/**
 * @author sergeys
 *
 * @constructor Creates a new Image
 */
class Image(path: String, flipY: Boolean = true) {
    val data: ByteBuffer
    val width: Int
    val height: Int
    val channels: Int
    
    init {
        val w = IntArray(1)
        val h = IntArray(1)
        val c = IntArray(1)
        STBImage.stbi_set_flip_vertically_on_load(flipY)
        data = STBImage.stbi_load(path, w, h, c, 0)!!
        width = w[0]
        height = h[0]
        channels = c[0]
    }
    
    fun free() {
        STBImage.stbi_image_free(data)
    }
    
    companion object
}

fun Image.createTexture(imageMode: Int, glMode: Int,
                        xMode: TextureWrapMode = TextureWrapMode.CLAMP_TO_EDGE,
                        yMode: TextureWrapMode = xMode,
                        clampColor: FloatArray? = null,
                        minInterp: TextureInterpolationMode = TextureInterpolationMode.BI_MIPMAP_LINEAR,
                        maxInterp: TextureInterpolationMode = TextureInterpolationMode.BI_MIPMAP_LINEAR,
                        generateMipmaps: Boolean = false): Texture2D {
    val texture2D = Texture2D(GL11.glGenTextures())
    
    texture2D.bound {
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, glMode, width, height, 0, imageMode, GL11.GL_UNSIGNED_BYTE, data)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, xMode.id)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, yMode.id)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minInterp.id)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, maxInterp.id)
        if (generateMipmaps) {
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
        }
        if (clampColor != null) {
            GL11.glTexParameterfv(GL11.GL_TEXTURE_2D, GL20.GL_TEXTURE_BORDER_COLOR, clampColor)
        }
    }
    
    return texture2D
}

fun Image.Companion.createTexture(path: String,
                                  imageMode: Int, glMode: Int,
                                  flipY: Boolean = true,
                                  xMode: TextureWrapMode = TextureWrapMode.CLAMP_TO_EDGE,
                                  yMode: TextureWrapMode = xMode,
                                  clampColor: FloatArray? = null,
                                  minInterp: TextureInterpolationMode = TextureInterpolationMode.BI_MIPMAP_LINEAR,
                                  maxInterp: TextureInterpolationMode = TextureInterpolationMode.BI_MIPMAP_LINEAR,
                                  generateMipmaps: Boolean = false): Texture2D {
    val image = Image(path, flipY)
    val texture = image.createTexture(imageMode, glMode, xMode, yMode, clampColor, minInterp, maxInterp, generateMipmaps)
    image.free()
    return texture
}
