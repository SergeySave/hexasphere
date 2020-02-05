package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainMajorFeature {
    fun compatibleWith(terrainType: TerrainType): Boolean
    
    object NoMajorFeature: TerrainMajorFeature {
        override fun compatibleWith(terrainType: TerrainType): Boolean = true
    }
    
    object ForestMajorFeature: TerrainMajorFeature {
        override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is LandTerrainType
    }
    
    object RainforestMajorFeature: TerrainMajorFeature {
        override fun compatibleWith(terrainType: TerrainType): Boolean = terrainType is LandTerrainType
    }
    //    object CityMajorFeature : TerrainMajorFeature, AbstractMajorFeature(2, 2, 2, 0) {
    //        override fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean = terrainType is LandTerrainType && terrainShape is LandTerrainShape
    //    }
}
