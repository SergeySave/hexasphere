package com.sergeysav.hexasphere.common.data

/**
 * @author sergeys
 *
 * @constructor Creates a new MutableBiMap
 */
class MutableBiMap<K, V> {
    private val forwardMap = mutableMapOf<K, V>()
    private val backwardMap = mutableMapOf<V, K>()
    
    fun put(key: K, value: V) {
        forwardMap[key] = value
        backwardMap[value] = key
    }
    
    fun getForward(key: K): V? = forwardMap[key]
    fun getBackward(value: V): K? = backwardMap[value]
    
    fun removeForward(key: K): V? {
        val value = forwardMap.remove(key)
        backwardMap.remove(value)
        return value
    }
    
    fun removeBackward(value: V): K? {
        val key = backwardMap.remove(value)
        forwardMap.remove(key)
        return key
    }
    
    fun clear() {
        forwardMap.clear()
        backwardMap.clear()
    }
}