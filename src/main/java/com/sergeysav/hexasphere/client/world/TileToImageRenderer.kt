package com.sergeysav.hexasphere.client.world

import com.sergeysav.hexasphere.client.bound
import com.sergeysav.hexasphere.client.gl.Framebuffer
import com.sergeysav.hexasphere.client.gl.GLDataUsage
import com.sergeysav.hexasphere.client.gl.GLDrawingMode
import com.sergeysav.hexasphere.client.gl.Mesh
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.client.gl.Texture2D
import com.sergeysav.hexasphere.client.gl.Vec2VertexAttribute
import com.sergeysav.hexasphere.client.gl.Vec3VertexAttribute
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
    private val floatsPerVert = 3 + 3 + 2
    private val vertices = FloatArray(floatsPerVert * 6)
    private val indices = IntArray(3 * 4)
    
    init {
        shaderProgram.createVertexShader(loadResource("/vertex.glsl"))
        shaderProgram.createFragmentShader(loadResource("/fragment.glsl"))
        shaderProgram.link()
        mesh = Mesh(GLDrawingMode.TRIANGLES, true)
        mesh.setVertices(vertices, GLDataUsage.DYNAMIC,
                         Vec3VertexAttribute("aPos"),
                         Vec3VertexAttribute("aColor"),
                         Vec2VertexAttribute("aUV"))
        for (j in 2 until 6) {
            indices[3 * (j - 2) + 0] = 0
            indices[3 * (j - 2) + 1] = j - 1
            indices[3 * (j - 2) + 2] = j
        }
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
            
            val (r, g, b) = tile.getColoring()
            val (u, v) = tile.getImageCoords()
            
            val numVerts = tile.tilePolygon.polygonType.vertices
            for (j in 0 until numVerts) {
                vertices[floatsPerVert * j + 0] = cos(j * 2f * PI / numVerts - PI / 2).toFloat()
                vertices[floatsPerVert * j + 1] = sin(j * 2f * PI / numVerts - PI / 2).toFloat()
                vertices[floatsPerVert * j + 2] = 0f
                vertices[floatsPerVert * j + 3] = r
                vertices[floatsPerVert * j + 4] = g
                vertices[floatsPerVert * j + 5] = b
                vertices[floatsPerVert * j + 6] = (0.25f - 0.25f * cos(
                        j * 2f * PI / numVerts + PI / 2).toFloat() + u / 2f)
                vertices[floatsPerVert * j + 7] = (0.25f + 0.25f * sin(
                        j * 2f * PI / numVerts + PI / 2).toFloat() + v / 2f)
            }
            
            mesh.setVertexData(vertices)
            
            framebuffer.bound {
                GL11.glViewport(0, 0, width, height)
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
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
        }
    }
}