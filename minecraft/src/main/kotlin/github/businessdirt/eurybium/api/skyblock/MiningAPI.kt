package github.businessdirt.eurybium.api.skyblock

import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI
import github.businessdirt.eurybium.data.ScoreboardData
import github.businessdirt.eurybium.data.model.IslandType
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
        get() = inDwarvenMines && isGlaciteTunnels(IslandType.DWARVEN_MINES, ScoreboardData.sidebarLinesFormatted)

    val inCrystalHollows: Boolean get() = !inMineshaft && IslandType.CRYSTAL_HOLLOWS.isInIsland()

    val inGoldMines: Boolean get() = !inMineshaft && IslandType.GOLD_MINES.isInIsland()

    val inDeepCaverns: Boolean get() = !inMineshaft && IslandType.DEEP_CAVERNS.isInIsland()

    /** Cold and the location line distinguish the tunnels and warm base camp from other Dwarven areas. */
    internal fun isGlaciteTunnels(island: IslandType, sidebar: List<String>): Boolean =
        island == IslandType.DWARVEN_MINES && sidebar.any { line ->
            '❄' in line || line.trim().startsWith("Cold:") ||
                ('⏣' in line && ("Glacite" in line || "Dwarven Base Camp" in line))
        }
}
