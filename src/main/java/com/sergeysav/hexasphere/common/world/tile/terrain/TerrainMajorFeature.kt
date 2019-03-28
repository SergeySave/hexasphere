package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainMajorFeature: TerrainBaseOutput, TerrainMovementRestrictor {
    fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean
    
    object NoMajorFeature: TerrainMajorFeature, AbstractTerrainMROutput("", 0, 0, 0, 0) {
        override fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean = true
    }
    
    object ForestMajorFeature: TerrainMajorFeature, AbstractTerrainMROutput("Forest", 1, 1, 0, 1) {
        override fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean = terrainShape is TerrainShape.FlatTerrainShape && terrainType is LandTerrainType
    }
    
    object RainforestMajorFeature: TerrainMajorFeature, AbstractTerrainMROutput("Rainforest", 1, 1, 0, 1) {
        override fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean = terrainShape is TerrainShape.FlatTerrainShape && terrainType is TerrainType.GrassTerrainType
    }
    //    object CityMajorFeature : TerrainMajorFeature, AbstractMajorFeature(2, 2, 2, 0) {
    //        override fun compatibleWith(terrainType: TerrainType, terrainShape: TerrainShape): Boolean = terrainType is LandTerrainType && terrainShape is LandTerrainShape
    //    }
}
