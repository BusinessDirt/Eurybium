package github.businessdirt.eurybium.api.skyblock

import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.events.CommandRegistrationEvent
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

    @HandleEvent
    private fun onCommandRegistrationEvent(event: CommandRegistrationEvent) = event.register("eybforcetogglemineshaft") {
        description = "Forcibly toggles being in a mineshaft. (JASP_1)"
        category = CommandCategory.DEVELOPER_TEST

        simpleCallback {
            currentMineshaft = if (currentMineshaft == null) MineshaftType.JASP_1 else null
            inMiningIsland = currentMineshaft != null
        }
    }
}
