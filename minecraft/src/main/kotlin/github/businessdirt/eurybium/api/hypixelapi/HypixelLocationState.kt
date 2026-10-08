package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.data.model.IslandType
import net.hypixel.data.type.GameType
import net.hypixel.data.type.ServerType

/**
 * Immutable location data from one published update.
 *
 * Retain a snapshot when scheduling work or reading several fields together. A guest-capable
 * island remains [IslandType.NONE] until its scoreboard confirms the owner/guest variant.
 *
 * @property inHypixel Whether a Hypixel hello or location packet has established the connection.
 * @property inAlpha Whether the hello packet reported a non-production environment.
 * @property serverId The API server identifier, or null when absent or disconnected.
 * @property serverType The API game/lobby type, or null when absent.
 * @property mode The API mode identifier used to resolve SkyBlock islands.
 * @property map Optional map metadata supplied by the location packet.
 * @property lobbyName Optional lobby identifier, such as `mainlobby12`.
 * @property island The confirmed island, NONE while absent/pending, or UNKNOWN for an unmapped mode.
 * @property skyBlockArea Plain scoreboard area name, or null before detection or after changing servers.
 * @property isGuest Whether the scoreboard confirmed a guest variant for the current island.
 */
data class HypixelLocationState(
    val inHypixel: Boolean = false,
    val inAlpha: Boolean = false,
    val serverId: String? = null,
    val serverType: ServerType? = null,
    val mode: String? = null,
    val map: String? = null,
    val lobbyName: String? = null,
    val island: IslandType = IslandType.NONE,
    val isGuest: Boolean = false,
    val skyBlockArea: String? = null,
) {
    /** True only when connected and the server reports the SkyBlock game type. */
    val inSkyBlock: Boolean get() = inHypixel && serverType == GameType.SKYBLOCK

    /** Non-null form of [serverId], returning an empty string when unavailable. */
    val serverName: String get() = serverId.orEmpty()

    val inLobby: Boolean get() = !lobbyName.isNullOrBlank()

    val inLimbo: Boolean get() = inHypixel && serverId == "limbo"

    /** Lobby identifier without its numeric suffix, or null if its format is not recognized. */
    val lobbyType: String?
        get() = lobbyName?.let { lobbyPattern.matchEntire(it)?.groups?.get("type")?.value }

    /** Tests membership only while connected to SkyBlock. */
    fun inAnyIsland(islands: Collection<IslandType>): Boolean = inSkyBlock && island in islands

    private companion object {
        val lobbyPattern = Regex("""(?<type>.*lobby)\d+""")
    }
}
