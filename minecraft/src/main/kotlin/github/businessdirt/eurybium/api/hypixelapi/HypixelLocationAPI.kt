package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI.inAnyIsland
import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI.state
import github.businessdirt.eurybium.api.repo.RepoPattern
import github.businessdirt.eurybium.data.ScoreboardData
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.RepoUpdateEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiJoinEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardTitleUpdateEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardUpdateEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import org.apache.logging.log4j.LogManager

/**
 * Read-only access to the current Hypixel location and its confirmed SkyBlock island.
 *
 * Individual getters read the latest snapshot. Capture [state] once when several fields
 * must describe the same update, particularly inside delayed work. Packet and scoreboard
 * handlers update location state before ordinary event listeners run.
 */
@EurybiumModule
object HypixelLocationAPI {
    private val logger = LogManager.getLogger(HypixelLocationAPI::class.java)
    private val tracker = HypixelLocationTracker { it.post() }

    /** Plain-text pattern; its own key prevents older cached color-based patterns from overriding the fallback. */
    private val skyBlockAreaPattern = RepoPattern("skyblock.area.plain", """\s*(?<symbol>[⏣ф])\s+(?<area>.+)""")

    /** A coherent immutable snapshot; retaining it does not track subsequent updates. */
    val state: HypixelLocationState get() = tracker.state

    val inHypixel get() = state.inHypixel

    val inSkyBlock get() = state.inSkyBlock

    val island get() = state.island

    /** Current plain SkyBlock area name, or null while unknown. */
    val skyBlockArea get() = state.skyBlockArea

    val serverId get() = state.serverId

    val serverName get() = state.serverName

    val inAlpha get() = state.inAlpha

    val serverType get() = state.serverType

    val mode get() = state.mode

    val map get() = state.map

    val lobbyName get() = state.lobbyName

    val lobbyType get() = state.lobbyType

    val inLobby get() = state.inLobby

    val inLimbo get() = state.inLimbo

    val isGuest get() = state.isGuest

    /** Whether the current connected SkyBlock location matches any supplied island. */
    fun inAnyIsland(vararg islandTypes: IslandType): Boolean = inAnyIsland(islandTypes.asList())

    /** Collection variant of [inAnyIsland], evaluated against one snapshot. */
    fun inAnyIsland(islandTypes: Collection<IslandType>): Boolean = state.inAnyIsland(islandTypes)

    @HandleEvent(priority = HandleEvent.HIGHEST)
    private fun onHypixelApiJoinEvent(event: HypixelApiJoinEvent) = tracker.joined(event.alpha)

    @HandleEvent(priority = HandleEvent.HIGHEST)
    private fun onHypixelApiServerChangeEvent(event: HypixelApiServerChangeEvent) {
        tracker.serverChanged(event)

        if (state.island == IslandType.UNKNOWN) {
            logger.warn("Unknown island mode detected: '{}'", event.mode)
        }
    }

    @HandleEvent(priority = HandleEvent.HIGHEST)
    private fun onScoreboardTitleUpdateEvent(event: ScoreboardTitleUpdateEvent) = tracker.scoreboardUpdated(event)

    @HandleEvent(priority = HandleEvent.HIGHEST)
    private fun onScoreboardUpdateEvent(event: ScoreboardUpdateEvent) =
        tracker.areaUpdated(event.newLines, skyBlockAreaPattern.resolve())

    // Recheck an unchanged scoreboard when repository data becomes available or its pattern changes.
    @HandleEvent(events = [ RepoUpdateEvent::class ], priority = HandleEvent.HIGHEST)
    private fun onRepoUpdate() = tracker.areaUpdated(ScoreboardData.sidebarLinesFormatted, skyBlockAreaPattern.resolve())

    @HandleEvent(events = [ ClientDisconnectEvent::class ], priority = HandleEvent.HIGHEST)
    private fun onDisconnect() = tracker.disconnected()
}
