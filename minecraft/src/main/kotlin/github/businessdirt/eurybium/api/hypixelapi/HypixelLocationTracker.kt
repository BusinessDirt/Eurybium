package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.core.utils.StringUtils.removeColor
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import github.businessdirt.eurybium.events.hypixel.HypixelLeaveEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardTitleUpdateEvent
import github.businessdirt.eurybium.events.skyblock.IslandJoinEvent
import github.businessdirt.eurybium.events.skyblock.IslandLeaveEvent
import net.hypixel.data.type.GameType

/**
 * Resolves location packets and scoreboard confirmations into island transitions.
 *
 * All mutation and [emit] calls belong to the client thread. Publishing a volatile immutable
 * [state] lets other threads read a coherent snapshot without synchronizing individual fields.
 */
internal class HypixelLocationTracker(private val emit: (EurybiumEvent) -> Unit) {
    /** The most recently committed snapshot; updated before its transition events are emitted. */
    @Volatile
    var state = HypixelLocationState()
        private set

    // API modes do not distinguish an owner island from its guest variant; the scoreboard does.
    private var pendingGuestIsland: IslandType? = null

    // Remember the last confirmed island through lobbies and pending confirmations within a connection.
    private var previousIsland = IslandType.NONE

    /** Marks the connection as Hypixel and records the hello packet environment. */
    fun joined(alpha: Boolean) {
        state = state.copy(inHypixel = true, inAlpha = alpha)
    }

    /** Applies packet metadata and either resolves the island or waits for a guest confirmation. */
    fun serverChanged(event: HypixelApiServerChangeEvent) {
        val detectedIsland = if (event.serverType == GameType.SKYBLOCK) {
            event.mode?.let(IslandType::getByIdOrUnknown) ?: IslandType.UNKNOWN
        } else {
            IslandType.NONE
        }

        val awaitingGuest = detectedIsland.takeIf { it.hasGuestVariant() }
        val next = HypixelLocationState(
            inHypixel = true,
            inAlpha = state.inAlpha,
            serverId = event.serverName,
            serverType = event.serverType,
            mode = event.mode,
            map = event.map,
            lobbyName = event.lobbyName,
            island = if (awaitingGuest != null) IslandType.NONE else detectedIsland,
        )

        // Metadata updates on the same server must not reset guest detection or emit another join.
        if (
            state.inHypixel &&
            state.serverId == next.serverId &&
            state.serverType == next.serverType &&
            state.mode == next.mode
        ) {
            state = state.copy(map = next.map, lobbyName = next.lobbyName)
            return
        }

        // NONE prevents consumers from mistaking the previous island for the unconfirmed destination.
        pendingGuestIsland = awaitingGuest
        changeLocation(next)
    }

    /** Confirms a pending owner/guest island once a SkyBlock scoreboard title is available. */
    fun scoreboardUpdated(event: ScoreboardTitleUpdateEvent) {
        val pending = pendingGuestIsland ?: return
        if (!state.inSkyBlock || !event.isSkyBlock) return

        val guest = event.title.removeColor().trim().endsWith("GUEST")
        pendingGuestIsland = null

        changeLocation(
            state.copy(
                island = if (guest) pending.guestVariant() else pending,
                isGuest = guest,
            )
        )
    }

    /** Clears metadata, pending confirmation, and history before emitting any disconnect events. */
    fun disconnected() {
        val wasConnected = state.inHypixel

        pendingGuestIsland = null
        previousIsland = IslandType.NONE
        changeLocation(HypixelLocationState())

        if (wasConnected) emit(HypixelLeaveEvent())
    }

    /** Commits state first, then emits leave before join using captured transition values. */
    private fun changeLocation(next: HypixelLocationState) {
        val old = state
        val previous = previousIsland

        state = next
        if (next.island != IslandType.NONE) previousIsland = next.island

        // Capture both event payloads before dispatch; listeners may trigger another transition.
        val events = buildList {
            if (old.island != IslandType.NONE) add(IslandLeaveEvent(old.island))
            if (next.island != IslandType.NONE) add(IslandJoinEvent(next.island, previous))
        }

        events.forEach(emit)
    }
}
