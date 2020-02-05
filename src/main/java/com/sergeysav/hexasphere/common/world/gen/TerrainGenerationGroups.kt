package com.sergeysav.hexasphere.common.world.gen

import com.sergeysav.hexasphere.common.chance
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainMajorFeature
import com.sergeysav.hexasphere.common.world.tile.terrain.TerrainType
import kotlin.random.Random

/**
 * @author sergeys
 *
 * @constructor Creates a new TerrainGenerationGroups
 */
data class TerrainGenerationGroup(
        val elevation: Float,
        val temperature: Float,
        val moisture: Float,
        val getter: (Random)->Pair<TerrainType, TerrainMajorFeature>
) {
    
    companion object {
        val land = listOf(
            TerrainGenerationGroup(0f, 1f, 1f) { random ->
                when {
                    random.chance(1 / 5.0) -> TerrainType.Hills to TerrainMajorFeature.NoMajorFeature
                    random.chance(1 / 4.0) -> TerrainType.Plains to TerrainMajorFeature.NoMajorFeature
                    else -> TerrainType.Plains to TerrainMajorFeature.RainforestMajorFeature
                }
            },
            TerrainGenerationGroup(0f, 0.5f, 0.5f) { random ->
                when {
                    random.chance(1 / 5.0) -> TerrainType.Hills to TerrainMajorFeature.NoMajorFeature
                    random.chance(1 / 4.0) -> TerrainType.Plains to TerrainMajorFeature.NoMajorFeature
                    else -> TerrainType.Plains to TerrainMajorFeature.ForestMajorFeature
                }
            },
            TerrainGenerationGroup(0f, 1f, 0f) { random ->
                when {
                    random.chance(0.5) -> TerrainType.Desert to TerrainMajorFeature.NoMajorFeature
                    else -> TerrainType.DesertHills to TerrainMajorFeature.NoMajorFeature
                }
            },
            TerrainGenerationGroup(0f, 0f, 0f) { random ->
                when {
                    random.chance(0.5) -> TerrainType.Tundra to TerrainMajorFeature.NoMajorFeature
                    else -> TerrainType.TundraHills to TerrainMajorFeature.NoMajorFeature
                }
            },
            TerrainGenerationGroup(0f, 1f, 0.7f) { random ->
                TerrainType.Desert to TerrainMajorFeature.ForestMajorFeature
            },
            TerrainGenerationGroup(0f, 0f, 0.7f) { random ->
                TerrainType.Tundra to TerrainMajorFeature.ForestMajorFeature
            }
        )
        
        val river = listOf(
            TerrainGenerationGroup(0f, 1f, 1f) { random ->
                when {
                    random.chance(1 / 5.0) -> TerrainType.HillsRiver to TerrainMajorFeature.NoMajorFeature
                    random.chance(1 / 4.0) -> TerrainType.PlainsRiver to TerrainMajorFeature.NoMajorFeature
                    else -> TerrainType.PlainsRiver to TerrainMajorFeature.RainforestMajorFeature
                }
            },
            TerrainGenerationGroup(0f, 0.5f, 0.5f) { random ->
                when {
                    random.chance(1 / 5.0) -> TerrainType.HillsRiver to TerrainMajorFeature.NoMajorFeature
                    random.chance(1 / 4.0) -> TerrainType.PlainsRiver to TerrainMajorFeature.NoMajorFeature
                    else -> TerrainType.PlainsRiver to TerrainMajorFeature.ForestMajorFeature
                }
            },
            TerrainGenerationGroup(0f, 1f, 0f) { random ->
                TerrainType.DesertRiver to TerrainMajorFeature.NoMajorFeature
            },
            TerrainGenerationGroup(0f, 0f, 0f) { random ->
                TerrainType.TundraRiver to TerrainMajorFeature.NoMajorFeature
            },
            TerrainGenerationGroup(0f, 1f, 0.7f) { random ->
                TerrainType.DesertRiver to TerrainMajorFeature.ForestMajorFeature
            },
            TerrainGenerationGroup(0f, 0f, 0.7f) { random ->
                TerrainType.TundraRiver to TerrainMajorFeature.ForestMajorFeature
            }
        )
    }
}