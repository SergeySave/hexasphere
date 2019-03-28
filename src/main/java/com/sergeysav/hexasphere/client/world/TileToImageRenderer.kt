package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.bound
import com.sergeysav.hexasphere.client.gl.Framebuffer
import com.sergeysav.hexasphere.client.gl.GLDataUsage
import com.sergeysav.hexasphere.client.gl.GLDrawingMode
import com.sergeysav.hexasphere.client.gl.Mesh
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.client.gl.Texture2D
import com.sergeysav.hexasphere.client.gl.bound
import com.sergeysav.hexasphere.common.LinAlgPool
import com.sergeysav.hexasphere.common.loadResource
import com.sergeysav.hexasphere.common.setUniform
import com.sergeysav.hexasphere.common.world.tile.Tile
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33C
import org.lwjgl.system.MemoryStack
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author sergeys
 *
 * @constructor Creates a new TileToImageRenderer
 */
class TileToImageRenderer(val width: Int, val height: Int, private val linAlgPool: LinAlgPool) {
    
    private val framebuffer = Framebuffer(GL33C.glGenFramebuffers())
    val texture2D = Texture2D(GL11.glGenTextures())
    private val shaderProgram = ShaderProgram()
    private val mesh: Mesh
    private val vertices = FloatArray(TileBaker.FLOATS_PER_VERT * 6)
    private val indices = IntArray(3 * 4)
    
    init {
        shaderProgram.createVertexShader(loadResource("/vertex.glsl"))
        shaderProgram.createFragmentShader(loadResource("/fragment.glsl"))
        shaderProgram.link()
        mesh = Mesh(GLDrawingMode.TRIANGLES, true)
        mesh.setVertices(vertices, GLDataUsage.DYNAMIC, *TileBaker.vertexAttributes)
        mesh.bound {
            shaderProgram.validate()
        }
        TileBaker.bakeTileIndices(6, indices, 0, 0)
        mesh.setIndexData(indices, GLDataUsage.STATIC)
        
        GL20.glUniform1i(shaderProgram.getUniform("texture1"), 0)
        
        framebuffer.bound {
            texture2D.bound {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGB,
                                  GL11.GL_UNSIGNED_BYTE, 0)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
                
                GL33C.glFramebufferTexture(GL33C.GL_FRAMEBUFFER, GL33C.GL_COLOR_ATTACHMENT0, texture2D.id, 0)
                GL33C.glDrawBuffers(GL33C.GL_COLOR_ATTACHMENT0)
                
                if (GL33C.glCheckFramebufferStatus(GL33C.GL_FRAMEBUFFER) != GL33C.GL_FRAMEBUFFER_COMPLETE) {
                    throw Exception("Framebuffer Setup Error")
                }
            }
        }
    }
    
    fun render(tile: Tile, worldRenderable: WorldRenderable) {
        MemoryStack.stackPush().use { stack ->
            val ints = stack.ints(0, 0, 0, 0)
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, ints)
            
            val numVerts = tile.tilePolygon.polygonType.vertices
            TileBaker.bakeTileVertices(tile, vertices, 0)
            for (j in 0 until numVerts) {
                vertices[TileBaker.FLOATS_PER_VERT * j + 0] = -cos(j * 2f * PI / numVerts - PI / 2).toFloat()
                vertices[TileBaker.FLOATS_PER_VERT * j + 1] = sin(j * 2f * PI / numVerts - PI / 2).toFloat()
                vertices[TileBaker.FLOATS_PER_VERT * j + 2] = 0f
            }
            
            mesh.setVertexData(vertices)
            
            framebuffer.bound {
                GL11.glViewport(0, 0, width, height)
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
                GL11.glCullFace(GL11.GL_FRONT)
                
                worldRenderable.texture.bound {
                    shaderProgram.bound {
                        linAlgPool.mat4 { mat4 ->
                            mat4.identity().setUniform(shaderProgram.getUniform("uCamera"))
                            mat4.identity().setUniform(shaderProgram.getUniform("uModel"))
                        }
                        mesh.draw(3 * (numVerts - 2))
                    }
                }
            }
            GL11.glViewport(ints[0], ints[1], ints[2], ints[3])
            GL11.glCullFace(GL11.GL_BACK)
        }
    }
}