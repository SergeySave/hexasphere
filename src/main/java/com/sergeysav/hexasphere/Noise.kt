package com.sergeysav.hexasphere

import org.joml.Random
import org.joml.SimplexNoise
import org.joml.Vector3fc

/**
 * @author sergeys
 */
fun noiseGenerator(seed: Long, octaves: Int = 1, aScaling: Float = 0.5f, fScaling: Float = 2.0f): (Vector3fc)->Float {
    val max = ((1.0 - Math.pow(aScaling.toDouble(), octaves.toDouble()))/(1 - aScaling)).toFloat()
    val r1 = Random(seed)
    val seeds = FloatArray(octaves) { (2 * r1.nextFloat() - 1) * 1e5f }
    return { pos ->
        var total = 0f
        var amp = 1f
        var freq = 1f
        for (i in 0 until octaves) {
            total += amp * SimplexNoise.noise(pos.x() * freq, pos.y() * freq, pos.z() * freq, seeds[i])
            amp *= aScaling
            freq *= fScaling
        }
        total / max
    }
}
