package com.sergeysav.hexasphere.client.assimp

import com.sergeysav.hexasphere.client.gl.Image
import com.sergeysav.hexasphere.client.gl.createTexture
import com.sergeysav.hexasphere.common.IOUtil
import com.sergeysav.hexasphere.common.fixPath
import org.lwjgl.assimp.AIMaterial
import org.lwjgl.assimp.AIString
import org.lwjgl.assimp.Assimp
import org.lwjgl.assimp.Assimp.aiProcess_Triangulate
import org.lwjgl.system.NativeType


/**
 * @author sergeys
 */
object AssimpUtils {
    
    private val textures = mutableMapOf<String, Pair<Int, ATexture>>()
    
    fun loadModel(filePath: String): AModel {
        val aiScene = Assimp.aiImportFile(IOUtil.getResourcePath(filePath),
                                          aiProcess_Triangulate or Assimp.aiProcess_JoinIdenticalVertices)
    
        if (aiScene == null || aiScene.mFlags() and Assimp.AI_SCENE_FLAGS_INCOMPLETE != 0 || aiScene.mRootNode() == null) {
            error(Assimp.aiGetErrorString() ?: "AssimpUtils.loadModel error")
        }
        
        val model = AModel(aiScene, "$filePath/..".fixPath())
        
        Assimp.aiReleaseImport(aiScene)
        
        return model
    }
    
    fun loadMaterialTextures(aiMaterial: AIMaterial, type: ATexture.Type, basePath: String): List<ATexture> {
        val textures = mutableListOf<ATexture>()
        for (i in 0 until aiMaterial.getTextureCount(type.assimpType)) {
            val path = aiMaterial.getTexture(type.assimpType, i)
            textures.add(loadTexture("$basePath/$path", type))
        }
        return textures
    }
    
    private fun loadTexture(path: String, type: ATexture.Type): ATexture = if (textures.containsKey(path)) {
        val (count, aTexture) = textures[path]!!
        textures[path] = (count + 1) to aTexture
        aTexture
    } else {
        val texture = Image.createTexture(path, generateMipmaps = true)
        val aTexture = ATexture(texture, path, type)
        textures[path] = 1 to aTexture
        aTexture
    }
    
    fun cleanupTexture(aTexture: ATexture) {
        if (textures.containsKey(aTexture.path)) {
            val (count, _) = textures[aTexture.path]!!
            if (count == 1) {
                textures.remove(aTexture.path)
                aTexture.texture2D.cleanup()
            } else {
                textures[aTexture.path] = (count - 1) to aTexture
            }
        }
    }
}

/**
 * Get the number of textures for a particular texture type.
 *
 * @param type Texture type to check for. One of:<br><table><tr><td>{@link #aiTextureType_NONE TextureType_NONE}</td><td>{@link #aiTextureType_DIFFUSE TextureType_DIFFUSE}</td><td>{@link #aiTextureType_SPECULAR TextureType_SPECULAR}</td><td>{@link #aiTextureType_AMBIENT TextureType_AMBIENT}</td></tr><tr><td>{@link #aiTextureType_EMISSIVE TextureType_EMISSIVE}</td><td>{@link #aiTextureType_HEIGHT TextureType_HEIGHT}</td><td>{@link #aiTextureType_NORMALS TextureType_NORMALS}</td><td>{@link #aiTextureType_SHININESS TextureType_SHININESS}</td></tr><tr><td>{@link #aiTextureType_OPACITY TextureType_OPACITY}</td><td>{@link #aiTextureType_DISPLACEMENT TextureType_DISPLACEMENT}</td><td>{@link #aiTextureType_LIGHTMAP TextureType_LIGHTMAP}</td><td>{@link #aiTextureType_REFLECTION TextureType_REFLECTION}</td></tr><tr><td>{@link #aiTextureType_UNKNOWN TextureType_UNKNOWN}</td></tr></table>
 *
 * @return Number of textures for this type.
 */
@NativeType("unsigned int")
fun AIMaterial.getTextureCount(@NativeType("aiTextureType") type: Int) = Assimp.aiGetMaterialTextureCount(this, type)

/** Array version of: {@link #aiGetMaterialTexture GetMaterialTexture} */
@NativeType("aiReturn")
fun AIMaterial.getTexture(@NativeType("aiTextureType") type: Int,
                          @NativeType("unsigned int") index: Int,
                          @NativeType("aiTextureMapping *") mapping: IntArray? = null,
                          @NativeType("unsigned int *") uvindex: IntArray? = null,
                          @NativeType("float *") blend: FloatArray? = null,
                          @NativeType("aiTextureOp *") op: IntArray? = null,
                          @NativeType("aiTextureMapMode *") mapmode: IntArray? = null,
                          @NativeType("unsigned int *") flags: IntArray? = null): String {
    val path = AIString.calloc()
    if (Assimp.aiGetMaterialTexture(this, type, index, path, mapping, uvindex, blend, op, mapmode, flags) != Assimp.aiReturn_SUCCESS) {
        path.free()
        error(Assimp.aiGetErrorString() ?: "AIMaterial.getTexture error")
    }
    val pathString = path.dataString()
    path.free()
    return pathString
}