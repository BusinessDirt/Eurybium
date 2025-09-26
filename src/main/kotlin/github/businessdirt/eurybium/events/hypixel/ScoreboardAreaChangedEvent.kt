package github.businessdirt.eurybium.events.hypixel

import github.businessdirt.eurybium.core.events.EurybiumEvent

class ScoreboardAreaChangedEvent(private val previousArea: String, private val currentArea: String) : EurybiumEvent()
