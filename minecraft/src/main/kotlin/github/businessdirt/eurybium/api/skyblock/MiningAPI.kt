package github.businessdirt.eurybium.api.skyblock

import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object MiningAPI {

    /** True on any mining island, including downloaded worlds with the mineshaft override enabled. */
    val inMiningIsland: Boolean
        get() = inMineshaft || HypixelLocationAPI.state.let { it.inSkyBlock && it.island.isMiningIsland() }

    val inMineshaft: Boolean get() = IslandType.MINESHAFT.isInIsland()

    /** Includes Glacite Tunnels, since they share the Dwarven Mines island. */
    val inDwarvenMines: Boolean get() = !inMineshaft && IslandType.DWARVEN_MINES.isInIsland()

    /** Includes the warm Dwarven Base Camp, but excludes ordinary Dwarven Mines locations. */
    val inGlaciteTunnels: Boolean
        get() = HypixelLocationAPI.let { locationAPI ->
            locationAPI.inAnyIsland(listOf(IslandType.DWARVEN_MINES)) &&
                locationAPI.skyBlockArea?.let { "Glacite" in it || it == "Dwarven Base Camp" } == true
        }

    val inCrystalHollows: Boolean get() = !inMineshaft && IslandType.CRYSTAL_HOLLOWS.isInIsland()

    val inGoldMines: Boolean get() = !inMineshaft && IslandType.GOLD_MINES.isInIsland()

    val inDeepCaverns: Boolean get() = !inMineshaft && IslandType.DEEP_CAVERNS.isInIsland()

    @HandleEvent
    private fun onCommandRegistrationEvent(event: CommandRegistrationEvent) = event.register("eybminingapidumpstate") {
        description = "Dumps the current state of the MiningAPI."
        category = CommandCategory.DEVELOPER_DEBUG

        simpleCallback {
            val stateMessage = "inMiningIsland=${inMiningIsland}, inMineshaft=${inMineshaft}, inDwarvenMines=${inDwarvenMines}, " +
                "inGlaciteTunnels=${inGlaciteTunnels}, inCrystalHollows=${inCrystalHollows}, " +
                "inGoldMines=${inGoldMines}, inDeepCaverns=${inDeepCaverns}"

            ChatAPI.debug("Current MiningAPI state:")
            ChatAPI.debug(stateMessage)
        }
    }
}
