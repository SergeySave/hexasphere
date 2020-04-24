package com.sergeysav.hexasphere.common.world.tile.terrain

/**
 * @author sergeys
 */
interface TerrainType {
    val name: String
    
    fun getWedgeResource(adjacent: TerrainType): String
    
    companion object {
        val types = listOf<TerrainType>(
                Ice, Coast, Ocean,
                Plains, PlainsRiver, Hills, HillsRiver,
                Mountain,
                Desert, DesertRiver, DesertHills,
                Tundra, TundraRiver, TundraHills
        )
    }
    
    object Ice : SimpleTerrainType("Ice", "/terrain/ice/base.obj"),
                 SeaTerrainType
    object Coast : SimpleTerrainType("Coast", "/terrain/coast/base.obj"),
                   SeaTerrainType
    object Ocean : SimpleTerrainType("Ocean", "/terrain/ocean/base.obj"),
                   SeaTerrainType
    object Plains : SimpleTerrainType("Plains", "/terrain/new_plains/plains.obj"),
                    LandTerrainType
    object PlainsRiver : AbstractRiverTerrainType("River Plains", "/terrain/plains_river/base/plains_river.obj", "/terrain/plains_river/side/plains_river.obj"),
                         RiverTerrainType, LandTerrainType
    object Hills : SimpleTerrainType("Hills", "/terrain/hills/hills.obj"),
                   LandTerrainType
    object HillsRiver : SimpleTerrainType("River Hills", "/terrain/hills/river.obj"),
                        RiverTerrainType, LandTerrainType
    object Mountain : SimpleTerrainType("Hills", "/terrain/mountains/mountains.obj")
    object Desert : SimpleTerrainType("Desert", "/terrain/desert/base.obj"),
                    LandTerrainType
    object DesertRiver : SimpleTerrainType("River Desert", "/terrain/desert/river.obj"),
                         LandTerrainType, RiverTerrainType
    object DesertHills : SimpleTerrainType("Desert Hills", "/terrain/desert_hills/base.obj"),
                         LandTerrainType
    object Tundra : SimpleTerrainType("Tundra", "/terrain/tundra/base.obj"),
                    LandTerrainType
    object TundraRiver : SimpleTerrainType("River Tundra", "/terrain/tundra/river.obj"),
                         LandTerrainType, RiverTerrainType
    object TundraHills : SimpleTerrainType("Tundra Hills", "/terrain/tundra_hills/base.obj"),
                         LandTerrainType
}

abstract class SimpleTerrainType(
        override val name: String,
        val tileResource: String
) : TerrainType {
    override fun getWedgeResource(adjacent: TerrainType) = tileResource
}

abstract class AbstractRiverTerrainType(
        override val name: String,
        val baseResource: String,
        val connectedResource: String
) : TerrainType, RiverTerrainType {
    override fun getWedgeResource(adjacent: TerrainType): String = if (adjacent is RiverTerrainType || adjacent is SeaTerrainType) {
        connectedResource
    } else {
        baseResource
    }
}

interface SeaTerrainType
interface LandTerrainType
interface RiverTerrainType
