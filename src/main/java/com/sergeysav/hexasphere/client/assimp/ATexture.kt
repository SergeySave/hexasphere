package com.sergeysav.hexasphere.client.assimp

import com.sergeysav.hexasphere.client.gl.Texture2D
import org.lwjgl.assimp.Assimp

/**
 * @author sergeys
 *
 * @constructor Creates a new ATexture
 */
data class ATexture(val texture2D: Texture2D, val path: String, val type: Type) {
    
    fun cleanup() {
        AssimpUtils.cleanupTexture(this)
    }
    
    enum class Type(val typeName: String, val assimpType: Int) {
        DIFFUSE("texture_diffuse", Assimp.aiTextureType_DIFFUSE),
        SPECULAR("texture_specular", Assimp.aiTextureType_SPECULAR),
        NORMAL("texture_normal", Assimp.aiTextureType_HEIGHT)
    }
}
