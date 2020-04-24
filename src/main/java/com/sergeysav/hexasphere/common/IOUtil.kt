package com.sergeysav.hexasphere.common

import com.sergeysav.hexasphere.Hexasphere
import org.lwjgl.BufferUtils
import org.lwjgl.BufferUtils.createByteBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths

object IOUtil {
    
    private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
        val newBuffer = BufferUtils.createByteBuffer(newCapacity)
        buffer.flip()
        newBuffer.put(buffer)
        return newBuffer
    }
    
    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    @Throws(IOException::class)
    fun readResourceToBuffer(resource: String, bufferSize: Int): ByteBuffer {
        var buffer: ByteBuffer = BufferUtils.createByteBuffer(0)
        
        val path = Paths.get(resource)
        if (Files.isReadable(path)) {
            Files.newByteChannel(path).use { fc ->
                buffer = BufferUtils.createByteBuffer(fc.size().toInt() + 1)
                while (fc.read(buffer) != -1) {
                }
            }
        } else {
            Hexasphere::class.java.getResourceAsStream(resource)?.use { source ->
                Channels.newChannel(source).use { rbc ->
                    buffer = createByteBuffer(bufferSize)
                    
                    while (true) {
                        val bytes = rbc.read(buffer)
                        if (bytes == -1) {
                            break
                        }
                        if (buffer.remaining() == 0) {
                            buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2) // 50%
                        }
                    }
                }
            } ?: error("Resource $resource not found")
        }
        
        buffer.flip()
        return buffer
    }
    
    fun getResourcePath(resource: String): String {
        val path = Paths.get(resource)
        return if (Files.isReadable(path)) {
            path.toAbsolutePath().toString()
        } else {
            Hexasphere::class.java.getResource(resource).path
        }
    }
    
    fun doesResourceExist(resource: String): Boolean = try {
        getResourcePath(resource)
        true
    } catch (e: IllegalStateException) {
        false
    }
}