package com.sergeysav.hexasphere.map

/**
 * @author sergeys
 */
enum class Biome(val r: Float, val g: Float, val b: Float) {
    MOUNTAIN(0.7f, 0.35f, 0.35f),
    RAINFOREST(0f, 0.5f, 0f),
    FOREST(0f, 0.9f, 0f),
    SAVANNA(0.5f, 0.8f, 0f),
    DESERT(0.8f, 0.8f, 0f),
    TUNDRA(0.3f, 0.3f, 0.5f),
    TAIGA(0.3f, 0.8f, 0.5f),
    ICE(0.6f, 0.6f, 1.0f),
    COAST(0.0f, 0.0f, 1.0f),
    OCEAN(0.0f, 0.0f, 0.5f),
    UNKNOWN(0f, 0f, 0f)
}