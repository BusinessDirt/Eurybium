package github.businessdirt.eurybium.events.skyblock

import github.businessdirt.eurybium.core.events.EurybiumEvent

class ScoreboardAreaChangedEvent(private val previousArea: String, private val currentArea: String) : EurybiumEvent()
