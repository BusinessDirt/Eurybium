package github.businessdirt.eurybium.events.hypixel

import github.businessdirt.eurybium.api.events.EurybiumEvent
import net.hypixel.data.type.ServerType

data class HypixelApiServerChangeEvent(
    val serverName: String?,
    val serverType: ServerType?,
    val lobbyName: String?,
    val mode: String?,
    val map: String?,
) : EurybiumEvent()
