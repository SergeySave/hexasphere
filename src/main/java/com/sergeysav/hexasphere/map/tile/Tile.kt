package com.sergeysav.hexasphere.map.tile

import com.sergeysav.hexasphere.map.tile.terrain.TerrainMajorFeature
import com.sergeysav.hexasphere.map.tile.terrain.TerrainMinorFeature
import com.sergeysav.hexasphere.map.tile.terrain.TerrainShape
import com.sergeysav.hexasphere.map.tile.terrain.TerrainType

/**
 * @author sergeys
 *
 * @constructor Creates a new Tile
 */
data class Tile(val tilePolygon: TilePolygon,
                val elevation: Float,
                val temperature: Float,
                val moisture: Float,
                val type: TerrainType,
                val shape: TerrainShape,
                val majorFeature: TerrainMajorFeature,
                val minorFeatures: Array<TerrainMinorFeature>) {
    
    fun determineColor(): Triple<Float, Float, Float> {
        var r = 0.0
        var g = 0.0
        var b = 0.0
        when (type) {
            is TerrainType.GrassTerrainType          -> g += 0.5
            is TerrainType.PermafrostTerrainType -> {
                r += 0.1
                g += 0.1
                b += 0.5
            }
            is TerrainType.SandTerrainType -> {
                r += 0.4
                g += 0.4
            }
            is TerrainType.WaterTerrainType -> b += 0.5
        }
        when (shape) {
            is TerrainShape.HillTerrainShape -> {
                r += 0.1
                g += 0.1
                b += 0.1
            }
            is TerrainShape.MountainTerrainShape -> r += 1.0
            is TerrainShape.OceanTerrainShape -> b += 0.1
            is TerrainShape.CoastTerrainShape -> b += 0.3
            is TerrainShape.IceTerrainShape -> {
                r += 0.2
                g += 0.2
                b += 0.1
            }
        }
        when (majorFeature) {
            is TerrainMajorFeature.ForestMajorFeature -> g += 0.2
            is TerrainMajorFeature.RainforestMajorFeature -> g += 0.4
        }
        return Triple(r.toFloat(), g.toFloat(), b.toFloat())
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tile) return false
        
        if (tilePolygon != other.tilePolygon) return false
        if (elevation != other.elevation) return false
        if (temperature != other.temperature) return false
        if (moisture != other.moisture) return false
//        if (biome != other.biome) return false
        if (type != other.type) return false
        if (shape != other.shape) return false
        if (majorFeature != other.majorFeature) return false
        if (!minorFeatures.contentEquals(other.minorFeatures)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = tilePolygon.hashCode()
        result = 31 * result + elevation.hashCode()
        result = 31 * result + temperature.hashCode()
        result = 31 * result + moisture.hashCode()
//        result = 31 * result + biome.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + majorFeature.hashCode()
        result = 31 * result + minorFeatures.contentHashCode()
        return result
    }
}