package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import github.businessdirt.eurybium.core.data.ScoreboardData
import github.businessdirt.eurybium.core.data.model.IslandType
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.events.hypixel.MineshaftEnteredEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.features.types.MineshaftType


@EurybiumModule
object MineshaftDetection {

    private var found = false

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        found = false
    }

    @HandleEvent(onlyOnIsland = IslandType.MINESHAFT)
    fun onSecondPassedEvent(event: SecondPassedEvent) {
        if (found) return

        val matchingLine = ScoreboardData.sidebarLinesFormatted
            .firstOrNull { line -> MineshaftType.entries.any { line.contains(it.name) } }
            ?: return

        val areaName = matchingLine.split(" ").last()


        val type = MineshaftType.entries.firstOrNull { areaName.contains(it.name) } ?: return
        found = true


        MineshaftEnteredEvent(type).post()
    }
}
