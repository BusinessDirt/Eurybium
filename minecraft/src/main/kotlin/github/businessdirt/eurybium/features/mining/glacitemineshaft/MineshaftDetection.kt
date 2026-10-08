package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.events.minecraft.ScoreboardUpdateEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.skyblock.GlaciteMineshaftDetectionEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object MineshaftDetection {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft

    private var found = false

    @HandleEvent(events = [ WorldChangeEvent::class ])
    private fun onWorldChangeEvent() {
        found = false
    }

    @HandleEvent(onIslands = [ IslandType.MINESHAFT ])
    private fun onScoreboardUpdateEvent(event: ScoreboardUpdateEvent) {
        if (found) return

        val matchingLine = event.newLines
            .firstOrNull { line -> MineshaftType.entries.any { line.contains(it.name) } }
            ?.removeColor() ?: return

        val areaName = matchingLine.split(" ").last()

        ChatAPI.debug("In area: $areaName")

        val type = MineshaftType.entries.firstOrNull { areaName.contains(it.name) } ?: return
        found = true

        ChatAPI.debug("Found a ${type.name} mineshaft! [$areaName]")
        GlaciteMineshaftDetectionEvent(type).post()
    }

    @HandleEvent
    private fun onGlaciteMineshaftDetectionEvent(event: GlaciteMineshaftDetectionEvent) {
        if (config.detectMineshaft) ChatAPI.chat("You entered a ${event.type.displayName} mineshaft!")
    }
}
