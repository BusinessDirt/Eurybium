package github.businessdirt.eurybium.events.hypixel

import github.businessdirt.eurybium.core.events.EurybiumEvent
import github.businessdirt.eurybium.core.data.model.IslandType

@Suppress("unused")
class SkyblockIslandChangeEvent(val oldIsland: IslandType, val newIsland: IslandType) : EurybiumEvent()

