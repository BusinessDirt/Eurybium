package github.businessdirt.eurybium.api.skyblock

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.minecraft.ScoreboardUpdateEvent
import github.businessdirt.eurybium.events.skyblock.IslandJoinEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object MiningAPI {

    @HandleEvent
    private fun onIslandJoinEvent(event: IslandJoinEvent) {
        if (event.island != IslandType.MINESHAFT) return

        EurybiumMod.logger.info("Detected Mineshaft")
    }

    @HandleEvent
    private fun onScoreboardUpdateEvent(event: ScoreboardUpdateEvent) {
        event.newLines.forEach { line ->
            EurybiumMod.logger.info(line)
        }
    }
}
