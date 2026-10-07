package github.businessdirt.eurybium.api.skyblock

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.events.minecraft.ScoreboardUpdateEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.skyblock.IslandJoinEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object MiningAPI {

    var inMiningIsland: Boolean = false
        private set

    var currentMineshaft: MineshaftType? = null
        private set

    @HandleEvent
    private fun onIslandJoinEvent(event: IslandJoinEvent) {
        inMiningIsland = event.island.isMiningIsland()
        if (!inMiningIsland) return
    }

    @HandleEvent
    private fun onScoreboardUpdateEvent(event: ScoreboardUpdateEvent) {

    }

    @HandleEvent(eventType = WorldChangeEvent::class)
    private fun onWorldChangeEvent() {
        inMiningIsland = false
        currentMineshaft = null
    }
}
