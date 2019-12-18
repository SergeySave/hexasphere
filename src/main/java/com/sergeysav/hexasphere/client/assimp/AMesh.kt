package com.sergeysav.hexasphere.client.assimp

import com.sergeysav.hexasphere.client.bound
import com.sergeysav.hexasphere.client.gl.ElementBufferObject
import com.sergeysav.hexasphere.client.gl.ShaderProgram
import com.sergeysav.hexasphere.client.gl.Vec2VertexAttribute
import com.sergeysav.hexasphere.client.gl.Vec3VertexAttribute
import com.sergeysav.hexasphere.client.gl.VertexArrayObject
import com.sergeysav.hexasphere.client.gl.VertexBufferObject
import org.lwjgl.assimp.AIMesh
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

/**
 * @author sergeys
 *
 * @constructor Creates a new AMesh
 */
class AMesh(aiMesh: AIMesh, vertexData: FloatArray, indexData: IntArray, private val textures: Array<ATexture>) {
    val vao: VertexArrayObject = VertexArrayObject()
    private val vbo: VertexBufferObject = VertexBufferObject()
    private val ebo: ElementBufferObject = ElementBufferObject()
    val indices = indexData.size
    
    init {
        vao.bound {
            vbo.bind()
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW)
            
            ebo.bind()
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexData, GL15.GL_STATIC_DRAW)
    
            // vertex positions
            GL30.glEnableVertexAttribArray(0)
            GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, vertexSize, 0L)
            // vertex normals
            GL30.glEnableVertexAttribArray(1)
            GL30.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, vertexSize, normalOffset.toLong())
            // vertex texture coords
            GL30.glEnableVertexAttribArray(2)
            GL30.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, vertexSize, texOffset.toLong())
        }
    }
    
    fun draw(shader: ShaderProgram, doDraw: Boolean = true) {
        var diffuseNr = 1
        var specularNr = 1
        
        for (i in textures.indices) {
            GL20.glActiveTexture(GL20.GL_TEXTURE0 + i)
            
            val name = textures[i].type.typeName
            val number = when (textures[i].type) {
                ATexture.Type.DIFFUSE -> diffuseNr++
                ATexture.Type.SPECULAR -> specularNr++
            }
            
            GL20.glUniform1i(shader.getUniform("$name$number"), i)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[i].texture2D.id)
        }
        
        if (doDraw) {
            vao.bound {
                GL20.glDrawElements(GL11.GL_TRIANGLES, indices, GL11.GL_UNSIGNED_INT, 0)
            }
        }
        
        GL20.glActiveTexture(GL20.GL_TEXTURE0)
    }
    
    fun cleanup() {
        vao.cleanup()
        vbo.cleanup()
        ebo.cleanup()
        textures.forEach { it.cleanup() }
    }
    
    companion object {
        private val normalOffset = Vec3VertexAttribute("position").totalLength
        private val texOffset = normalOffset + Vec3VertexAttribute("normal").totalLength
        private val vertexSize = texOffset + Vec2VertexAttribute("texCoords").totalLength
        val floatsPerVertex = vertexSize / 4;
    }
}