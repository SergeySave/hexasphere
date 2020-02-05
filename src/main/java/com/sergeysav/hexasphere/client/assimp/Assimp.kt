package com.sergeysav.hexasphere.client.assimp

import com.sergeysav.hexasphere.client.gl.Image
import com.sergeysav.hexasphere.client.gl.TextureInterpolationMode
import com.sergeysav.hexasphere.client.gl.TextureWrapMode
import com.sergeysav.hexasphere.client.gl.createTexture
import com.sergeysav.hexasphere.common.IOUtil
import com.sergeysav.hexasphere.common.fixPath
import mu.KotlinLogging
import org.lwjgl.assimp.AIFile
import org.lwjgl.assimp.AIFileCloseProc
import org.lwjgl.assimp.AIFileCloseProcI
import org.lwjgl.assimp.AIFileIO
import org.lwjgl.assimp.AIFileOpenProc
import org.lwjgl.assimp.AIFileOpenProcI
import org.lwjgl.assimp.AIFileReadProc
import org.lwjgl.assimp.AIFileReadProcI
import org.lwjgl.assimp.AIFileSeek
import org.lwjgl.assimp.AIFileSeekI
import org.lwjgl.assimp.AIFileTellProc
import org.lwjgl.assimp.AIFileTellProcI
import org.lwjgl.assimp.AIMaterial
import org.lwjgl.assimp.AIString
import org.lwjgl.assimp.Assimp
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.NativeType
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.min


/**
 * @author sergeys
 */
object AssimpUtils {
    
    private val logger = KotlinLogging.logger {  }
    private val textures = mutableMapOf<String, Pair<Int, ATexture>>()
    
    fun loadTileMesh(filePath: String): AMesh {
        if (!IOUtil.doesResourceExist(filePath)) {
            logger.info { "Tile Resource Does Not Exist: $filePath" }
            return loadTileMesh("/missing/missing.obj") // Load the missing tile if the right one does not exist
        }
        val model = loadModel(filePath)
        if (model.meshes.size == 0) {
            logger.error { "Tile Resource is missing a model: $filePath" }
            return loadTileMesh("/missing/missing.obj") // Load the missing tile if the right one does not exist
        }
        if (model.meshes.size > 1) {
            logger.warn { "Tile Resource has multiple meshes: $filePath. Using the first mesh." }
        }
        return model.meshes[0]
    }
    
    fun loadModel(filePath: String): AModel {
        logger.trace { "Loading Model: $filePath" }
        val fileIo = AIFileIO.create()
        val fileOpenProc: AIFileOpenProcI = object: AIFileOpenProc() {
            override fun invoke(pFileIO: Long, fileName: Long, openMode: Long): Long {
                val aiFile = AIFile.create()
                val data: ByteBuffer
                val fileNameUtf8: String = MemoryUtil.memUTF8(fileName)
                try {
                    logger.trace { "Assimp Loading Resource: $fileNameUtf8" }
                    data = IOUtil.readResourceToBuffer(fileNameUtf8, 8192)
                } catch (e: IOException) {
                    throw RuntimeException("Could not open file: $fileNameUtf8")
                }
                val fileReadProc: AIFileReadProcI = object: AIFileReadProc() {
                    override fun invoke(pFile: Long, pBuffer: Long, size: Long, count: Long): Long {
                        val max = min(data.remaining().toLong(), size * count)
                        MemoryUtil.memCopy(MemoryUtil.memAddress(data) + data.position(), pBuffer, max)
                        return max
                    }
                }
                val fileSeekProc: AIFileSeekI = object: AIFileSeek() {
                    override fun invoke(pFile: Long, offset: Long, origin: Int): Int {
                        if (origin == Assimp.aiOrigin_CUR) {
                            data.position(data.position() + offset.toInt())
                        } else if (origin == Assimp.aiOrigin_SET) {
                            data.position(offset.toInt())
                        } else if (origin == Assimp.aiOrigin_END) {
                            data.position(data.limit() + offset.toInt())
                        }
                        return 0
                    }
                }
                val fileTellProc: AIFileTellProcI = object: AIFileTellProc() {
                    override fun invoke(pFile: Long): Long {
                        return data.limit().toLong()
                    }
                }
                aiFile.ReadProc(fileReadProc)
                aiFile.SeekProc(fileSeekProc)
                aiFile.FileSizeProc(fileTellProc)
                return aiFile.address()
            }
        }
        val fileCloseProc: AIFileCloseProcI = object: AIFileCloseProc() {
            override fun invoke(pFileIO: Long, pFile: Long) { /* Nothing to do */
            }
        }
        fileIo[fileOpenProc, fileCloseProc] = MemoryUtil.NULL
        val aiScene = Assimp.aiImportFileEx(filePath,
                                            Assimp.aiProcess_JoinIdenticalVertices or
                                                    Assimp.aiProcess_Triangulate or
                                                    Assimp.aiProcess_CalcTangentSpace/* or
                                                    Assimp.aiProcess_OptimizeMeshes*/,
                                            fileIo)
     
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
        if (IOUtil.doesResourceExist(path)) {
            val texture = if (path == "/MissingImage.png") {
                Image.createTexture(path,
                                    generateMipmaps = false,
                                    minInterp = TextureInterpolationMode.NEAREST,
                                    maxInterp = TextureInterpolationMode.NEAREST,
                                    xMode = TextureWrapMode.REPEAT)
            } else {
                Image.createTexture(path, generateMipmaps = true)
            }
            val aTexture = ATexture(texture, path, type)
            textures[path] = 1 to aTexture
            aTexture
        } else {
            logger.info { "Texture Resource Does Not Exist: $path" }
            val aTexture = loadTexture("/MissingImage.png", ATexture.Type.DIFFUSE)
            ATexture(aTexture.texture2D, aTexture.path, type)
        }
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