package com.sergeysav.hexasphere

import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * @author sergeys
 */
class LinAlgPool {
    val vec2Pool = Pool(0) { Vector2f() }
    val vec3Pool = Pool(0) { Vector3f() }
    val vec4Pool = Pool(0) { Vector4f() }
    val mat3Pool = Pool(0) { Matrix3f() }
    val mat4Pool = Pool(0) { Matrix4f() }
    
    inline fun <reified T> vec2(inner: (Vector2f)->T) = vec2Pool.using(inner)
    inline fun <reified T> vec3(inner: (Vector3f)->T) = vec3Pool.using(inner)
    inline fun <reified T> vec4(inner: (Vector4f)->T) = vec4Pool.using(inner)
    inline fun <reified T> mat3(inner: (Matrix3f)->T) = mat3Pool.using(inner)
    inline fun <reified T> mat4(inner: (Matrix4f)->T) = mat4Pool.using(inner)
    
    inner class Pool<T>(initialSize: Int, private val factory: ()->T) {
        private val pool = MutableList(initialSize) { factory() }
        
        fun take(): T = if (pool.isEmpty()) {
            factory()
        } else {
            pool.removeAt(pool.size - 1)
        }
        
        fun give(item: T) {
            pool.add(item)
        }
        
        inline fun <reified U> using(inner: (T)->U): U {
            val t = take()
            return try {
                inner(t)
            } finally {
                give(t)
            }
        }
    }
}