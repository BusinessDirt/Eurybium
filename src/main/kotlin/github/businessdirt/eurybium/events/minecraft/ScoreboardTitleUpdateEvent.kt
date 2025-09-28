package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.core.events.EurybiumEvent

@Suppress("unused")
class ScoreboardTitleUpdateEvent(val objectiveName: String, val title: String) : EurybiumEvent() {
    val isSkyblock: Boolean
        get() = this.objectiveName == "SBScoreboard"
}
