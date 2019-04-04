package com.sergeysav.hexasphere.common.world.tile

import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMajorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMinorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainShape
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainType

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
    
    fun getColoring(): Triple<Float, Float, Float> {
        if (minorFeatures.contains(TerrainMinorFeature.RiverFeature)) {
            return Triple(0f, 0f, 0f)
        }
        return when (type) {
            is TerrainType.GrassTerrainType      -> Triple(0f, 1f - when (majorFeature) {
                is TerrainMajorFeature.ForestMajorFeature     -> 0.2f
                is TerrainMajorFeature.RainforestMajorFeature -> 0.4f
                else                                          -> 0f
            }, 0f)
            is TerrainType.PermafrostTerrainType -> Triple(1f, 1f - when (majorFeature) {
                is TerrainMajorFeature.ForestMajorFeature -> 0.2f
                else                                      -> 0f
            }, 1f)
            is TerrainType.SandTerrainType       -> Triple(0.5f, 0.5f + when (majorFeature) {
                is TerrainMajorFeature.ForestMajorFeature -> 0.2f
                else                                      -> 0f
            }, 0f)
            is TerrainType.WaterTerrainType      -> when (shape) {
                is TerrainShape.CoastTerrainShape -> Triple(0f, 0f, 1f)
                is TerrainShape.OceanTerrainShape -> Triple(0f, 0f, 0.5f)
                is TerrainShape.IceTerrainShape   -> Triple(0.5f, 0.5f, 1f)
                else                              -> Triple(0f, 0f, 0f)
            }
            is TerrainType.MountainTerrainType   -> Triple(0.5f, 0.5f, 0.5f)
            else                                 -> Triple(0f, 0f, 0f)
        }
    }
    
    fun getImageCoords(): Pair<Int, Int> = when (shape) {
        is TerrainShape.MountainTerrainShape -> 0 to 1
        is TerrainShape.HillTerrainShape     -> 1 to 1
        is TerrainShape.FlatTerrainShape     -> 0 to 0
        else                                 -> 1 to 0
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