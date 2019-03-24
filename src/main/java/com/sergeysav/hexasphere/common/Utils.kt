package com.sergeysav.hexasphere.common

import com.sergeysav.hexasphere.Hexasphere
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryStack
import java.io.File
import java.util.Scanner
import kotlin.random.Random

/**
 * @author sergeys
 */
fun loadResource(fileName: String): String {
    Hexasphere::class.java.getResourceAsStream(fileName).use {
        Scanner(it, "UTF-8").use { scan ->
            scan.useDelimiter("\\A")
            return scan.next()
        }
    }
}

fun getResourcePath(fileName: String): String = File(Hexasphere::class.java.getResource(fileName).toURI()).absolutePath

fun Matrix3f.setUniform(uniformId: Int) {
    MemoryStack.stackPush().use { stack ->
        // Dump the matrix into a float buffer
        val fb = stack.mallocFloat(9)
        this.get(fb)
        GL20.glUniformMatrix3fv(uniformId, false, fb)
    }
}

fun Matrix4f.setUniform(uniformId: Int) {
    MemoryStack.stackPush().use { stack ->
        // Dump the matrix into a float buffer
        val fb = stack.mallocFloat(16)
        this.get(fb)
        GL20.glUniformMatrix4fv(uniformId, false, fb)
    }
}

fun Random.chance(p: Double) = this.nextDouble() <= p

val ZERO: Vector3fc = Vector3f(0f, 0f, 0f)