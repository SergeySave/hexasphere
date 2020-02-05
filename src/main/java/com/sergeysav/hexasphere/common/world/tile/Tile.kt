package com.sergeysav.hexasphere.common.world.tile

import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMajorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMinorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainType
import org.joml.Vector3f

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
                val majorFeature: TerrainMajorFeature,
                val minorFeatures: Array<TerrainMinorFeature>) {
    
    lateinit var adjacent: Array<Tile>
        private set
    
    internal fun setAdjacent(adjacent: Array<Tile>) {
        val vector3f = Vector3f()
        val adj = Array(tilePolygon.wedges.size) {
            val wedge = tilePolygon.wedges[it]
            adjacent.minBy { a ->
                a.tilePolygon.vertices.asSequence().map { v -> v.sub(wedge.vertex1, vector3f); vector3f.lengthSquared() }.min()!! +
                a.tilePolygon.vertices.asSequence().map { v -> v.sub(wedge.vertex2, vector3f); vector3f.lengthSquared() }.min()!!
            }!!
        }
        this.adjacent = adj
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
//        if (shape != other.shape) return false
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
//        result = 31 * result + shape.hashCode()
        result = 31 * result + majorFeature.hashCode()
        result = 31 * result + minorFeatures.contentHashCode()
        return result
    }
}