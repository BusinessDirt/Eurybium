package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.core.events.EurybiumEvent

@Suppress("unused")
class ScoreboardUpdateEvent(val newLines: List<String>, val oldLines: List<String>) : EurybiumEvent() {
    val added: List<String> = newLines - oldLines.toSet()
    val removed: List<String> = oldLines - newLines.toSet()
}
