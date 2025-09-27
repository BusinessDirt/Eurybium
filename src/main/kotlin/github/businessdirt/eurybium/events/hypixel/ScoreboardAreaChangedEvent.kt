package github.businessdirt.eurybium.events.hypixel

import github.businessdirt.eurybium.core.events.EurybiumEvent

class ScoreboardAreaChangedEvent(val previousArea: String, val currentArea: String) : EurybiumEvent()
