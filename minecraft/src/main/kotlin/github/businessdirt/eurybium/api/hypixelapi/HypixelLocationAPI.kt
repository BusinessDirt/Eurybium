package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI.inAnyIsland
import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI.state
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.hypixel.HypixelApiJoinEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardTitleUpdateEvent
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

    /** A coherent immutable snapshot; retaining it does not track subsequent updates. */
    val state: HypixelLocationState get() = tracker.state

    val inHypixel get() = state.inHypixel

    val inSkyBlock get() = state.inSkyBlock

    val island get() = state.island

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

    @HandleEvent(eventType = ClientDisconnectEvent::class, priority = HandleEvent.HIGHEST)
    private fun onDisconnect() = tracker.disconnected()
}
