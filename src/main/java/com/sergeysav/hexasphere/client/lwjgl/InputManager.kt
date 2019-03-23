package com.sergeysav.hexasphere.client.lwjgl

import java.nio.ByteBuffer
import java.util.PriorityQueue

/**
 * @author sergeys
 *
 * @constructor Creates a new InputManager
 */
abstract class InputManager {
    val keyCallbacks = CallbackList<(key: Int, scancode: Int, action: Int, mods: Int) -> Boolean>()
    val mouseButtonCallbacks = CallbackList<(button: Int, action: Int, mods: Int, x: Double, y: Double) -> Boolean>()
    val mouseMoveCallbacks = CallbackList<(x: Double, y: Double) -> Boolean>()
    val scrollCallbacks = CallbackList<(x: Double, y: Double) -> Boolean>()
    val characterCallbacks = CallbackList<(codePoint: Int) -> Boolean>()
    
    fun handleKeyCallback(key: Int, scancode: Int, action: Int, mods: Int) {
        for ((callback, _) in keyCallbacks.callbacks) {
            if (callback(key, scancode, action, mods)) return
        }
    }
    
    fun handleMouseButtonCallback(button: Int, action: Int, mods: Int, x: Double, y: Double) {
        for ((callback, _) in mouseButtonCallbacks.callbacks) {
            if (callback(button, action, mods, x, y)) return
        }
    }
    
    fun handleMouseMoveCallback(x: Double, y: Double) {
        for ((callback, _) in mouseMoveCallbacks.callbacks) {
            if (callback(x, y)) return
        }
    }
    
    fun handleScrollCallback(x: Double, y: Double) {
        for ((callback, _) in scrollCallbacks.callbacks) {
            if (callback(x, y)) return
        }
    }
    
    fun handleCharacterCallback(codePoint: Int) {
        for ((callback, _) in characterCallbacks.callbacks) {
            if (callback(codePoint)) return
        }
    }
    
    abstract fun isKeyPressed(key: Int): Boolean
    abstract fun setClipboardString(string: ByteBuffer)
    abstract fun getClipboardString(): Long
    abstract fun checkForInputEvents()
    abstract fun setInputMode(mode: Int, value: Int)
    abstract fun setCursorPosition(x: Double, y: Double)
    
    class CallbackList<T> {
        internal val callbacks = PriorityQueue<Pair<T, Int>> { p1, p2 -> p1.second.compareTo(p2.second) }
        
        fun add(priority: Int = 0, callback: T) = callbacks.add(callback to priority)
        
        fun remove(callback: T) = callbacks.removeIf { it.first == callback }
    }
}