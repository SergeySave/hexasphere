package com.sergeysav.hexasphere.client.assimp

import com.sergeysav.hexasphere.client.gl.ShaderProgram
import org.lwjgl.assimp.AIMaterial
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.AIScene

/**
 * @author sergeys
 *
 * @constructor Creates a new AModel
 */
class AModel(scene: AIScene, basePath: String) {
    val meshes = mutableListOf<AMesh>()
    
    init {
        for (i in 0 until scene.mNumMeshes()) {
            meshes.add(processMesh(AIMesh.create(scene.mMeshes()!![i]), scene, basePath))
        }
    }
    
    fun draw(shaderProgram: ShaderProgram) {
        meshes.forEach { it.draw(shaderProgram, true) }
    }
    
    fun cleanup() {
        meshes.forEach { it.cleanup() }
    }
    
    private fun processMesh(aiMesh: AIMesh, scene: AIScene, basePath: String): AMesh {
        val vertices = FloatArray(aiMesh.mNumVertices() * AMesh.floatsPerVertex)
        val indices = IntArray(aiMesh.mNumFaces() * 3)
        val textures = mutableListOf<ATexture>()
        
        for (i in 0 until aiMesh.mNumVertices()) {
            vertices[i * AMesh.floatsPerVertex + 0] = aiMesh.mVertices()[i].x()
            vertices[i * AMesh.floatsPerVertex + 1] = aiMesh.mVertices()[i].y()
            vertices[i * AMesh.floatsPerVertex + 2] = aiMesh.mVertices()[i].z()
            vertices[i * AMesh.floatsPerVertex + 3] = aiMesh.mNormals()!![i].x()
            vertices[i * AMesh.floatsPerVertex + 4] = aiMesh.mNormals()!![i].y()
            vertices[i * AMesh.floatsPerVertex + 5] = aiMesh.mNormals()!![i].z()
            if (aiMesh.mTextureCoords()[0] != 0L) {
                val texCoords = aiMesh.mTextureCoords(0)!![i]
                vertices[i * AMesh.floatsPerVertex + 6] = texCoords.x()
                vertices[i * AMesh.floatsPerVertex + 7] = texCoords.y()
            } else {
                vertices[i * AMesh.floatsPerVertex + 6] = 0f
                vertices[i * AMesh.floatsPerVertex + 7] = 0f
            }
        }
        
        for (i in 0 until aiMesh.mNumFaces()) {
            val aiFace = aiMesh.mFaces()[i]
            for (j in 0 until aiFace.mNumIndices()) {
                indices[i * 3 + j] = aiFace.mIndices()[j]
            }
        }
        
        if (aiMesh.mMaterialIndex() >= 0) {
            val aiMaterial = AIMaterial.create(scene.mMaterials()!![aiMesh.mMaterialIndex()])
            textures.addAll(AssimpUtils.loadMaterialTextures(aiMaterial, ATexture.Type.DIFFUSE, basePath))
            textures.addAll(AssimpUtils.loadMaterialTextures(aiMaterial, ATexture.Type.SPECULAR, basePath))
        }
        
        return AMesh(aiMesh, vertices, indices, textures.toTypedArray())
    }
}