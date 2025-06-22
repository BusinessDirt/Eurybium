package github.businessdirt.eurybium.events.skyblock

import github.businessdirt.eurybium.core.events.EurybiumEvent
import github.businessdirt.eurybium.data.model.IslandType

class SkyblockIslandChangeEvent(private val oldIsland: IslandType, private val newIsland: IslandType) : EurybiumEvent()

