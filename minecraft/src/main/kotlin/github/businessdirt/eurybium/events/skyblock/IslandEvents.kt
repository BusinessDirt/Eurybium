package github.businessdirt.eurybium.events.skyblock

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.data.model.IslandType

class IslandJoinEvent(val island: IslandType, val previousIsland: IslandType) : EurybiumEvent()
class IslandLeaveEvent(val island: IslandType) : EurybiumEvent()
