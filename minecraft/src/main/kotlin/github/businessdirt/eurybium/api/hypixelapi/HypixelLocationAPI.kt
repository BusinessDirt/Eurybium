package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.Chat
import github.businessdirt.eurybium.core.utils.DelayedRun
import github.businessdirt.eurybium.core.utils.RegexUtils.matchMatcher
import github.businessdirt.eurybium.core.utils.StringUtils.removeColor
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.skyblock.IslandJoinEvent
import github.businessdirt.eurybium.events.skyblock.IslandLeaveEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiJoinEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import github.businessdirt.eurybium.events.hypixel.HypixelLeaveEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardTitleUpdateEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.hypixel.data.type.GameType
import net.hypixel.data.type.ServerType
import org.apache.logging.log4j.LogManager
import java.util.regex.Pattern

@Suppress("MemberVisibilityCanBePrivate")
@EurybiumModule
object HypixelLocationAPI {

    private val lobbyTypePattern = Pattern.compile("(?<lobbyType>.*lobby)\\\\d+")

    var inHypixel: Boolean = false
        private set

    var inSkyBlock: Boolean = false
        private set

    var island: IslandType = IslandType.NONE
        private set

    var serverId: String? = null
        private set

    val serverName get() = serverId.orEmpty()

    var inAlpha: Boolean = false
        private set

    var serverType: ServerType? = null
        private set

    var mode: String? = null
        private set

    var map: String? = null
        private set

    var lobbyName: String? = null
        private set

    var lobbyType: String? = null
        private set

    val inLobby get() = !lobbyName.isNullOrEmpty()
    val inLimbo get() = serverId == "limbo"

    var isGuest: Boolean = false
        private set

    private val logger = LogManager.getLogger(HypixelLocationAPI::class.java)

    private var sentIslandEvent = false
    private var internalIsland = IslandType.NONE
    private var previousIsland = IslandType.NONE

    fun inAnyIsland(vararg islandTypes: IslandType): Boolean = inHypixel && inSkyBlock && islandTypes.any { it == island }
    fun inAnyIsland(islandTypes: Collection<IslandType>): Boolean = inHypixel && inSkyBlock && islandTypes.contains(island)

    @HandleEvent(priority = HandleEvent.HIGHEST)
    private fun onHypixelApiJoinEvent(event: HypixelApiJoinEvent) {
        inAlpha = event.alpha
        inHypixel = true
    }

    @HandleEvent(priority = HandleEvent.HIGHEST)
    private fun onHypixelApiServerChangeEvent(event: HypixelApiServerChangeEvent) {
        inHypixel = true
        inSkyBlock = event.serverType == GameType.SKYBLOCK
        serverType = event.serverType
        mode = event.mode
        map = event.map
        serverId = event.serverName
        lobbyName = event.lobbyName

        lobbyType = event.lobbyName?.let { name ->
            lobbyTypePattern.matchMatcher(name) {
                group("lobbyType")
            }
        }

        isGuest = false

        // Set island to NONE when you leave skyblock
        if (!inSkyBlock) {
            internalIsland = IslandType.NONE
            changeIsland()
            return
        }

        val mode = event.mode ?: return

        val newIsland = IslandType.getByIdOrUnknown(mode)
        if (newIsland == IslandType.UNKNOWN) {
            Chat.debug("Unknown island mode detected: '$mode'")
            logger.warn("Unknown island mode detected: '$mode'")
        } else {
            logger.debug("Island detected: '{}'", newIsland)
        }

        internalIsland = newIsland

        // If the island has a guest variant, we wait for the scoreboard packet to confirm if it's a guest island or not
        if (internalIsland.hasGuestVariant()) {
            sentIslandEvent = false
        } else {
            sentIslandEvent = true
            changeIsland()
        }
    }

    @HandleEvent
    private fun onScoreboardTitleUpdateEvent(event: ScoreboardTitleUpdateEvent) {
        if (!inHypixel || !inSkyBlock || sentIslandEvent || !event.isSkyBlock) return

        isGuest = event.title.trim().removeColor().endsWith("GUEST")
        sentIslandEvent = true

        if (internalIsland.hasGuestVariant() && isGuest) internalIsland = internalIsland.guestVariant()

        changeIsland()
    }

    private fun changeIsland() {
        val oldIsland = island
        island = internalIsland
        logger.debug("Island change: '{}' -> '{}'", oldIsland, island)

        if (oldIsland != IslandType.NONE) {
            DelayedRun.runOrNextTick {
                IslandLeaveEvent(oldIsland).post()
            }
        }

        if (island != IslandType.NONE) {
            val captured = previousIsland
            DelayedRun.runOrNextTick {
                IslandJoinEvent(island = island, previousIsland = captured).post()
            }
            previousIsland = island
        }
    }

    @HandleEvent(eventType = ClientDisconnectEvent::class)
    private fun onDisconnect() {
        if (inSkyBlock || island != IslandType.NONE) {
            internalIsland = IslandType.NONE
            changeIsland()
        }

        val wasInHypixel = inHypixel
        reset()
        if (wasInHypixel) HypixelLeaveEvent().post()
    }

    private fun reset() {
        inHypixel = false
        inSkyBlock = false
        island = IslandType.NONE
        serverId = null
        inAlpha = false
        serverType = null
        mode = null
        map = null
        lobbyName = null
        lobbyType = null
        isGuest = false
        sentIslandEvent = false
        internalIsland = IslandType.NONE
    }
}
