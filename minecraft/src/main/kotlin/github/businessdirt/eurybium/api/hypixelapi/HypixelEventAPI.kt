package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.events.hypixel.HypixelApiJoinEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.hypixel.data.region.Environment
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundVersionedPacket
import kotlin.jvm.optionals.getOrNull

@EurybiumModule
object HypixelEventAPI {

    private val modApi: HypixelModAPI = HypixelModAPI.getInstance()

    init {
        modApi.subscribeToEventPacket(ClientboundLocationPacket::class.java)
        modApi.createHandler(ClientboundHelloPacket::class.java, ::onHelloPacket)
        modApi.createHandler(ClientboundLocationPacket::class.java, ::onLocationPacket)
    }

    private fun onHelloPacket(packet: ClientboundHelloPacket) {
        val isAlpha = packet.environment != Environment.PRODUCTION
        HypixelApiJoinEvent(isAlpha).post()
    }

    private fun onLocationPacket(packet: ClientboundLocationPacket) {
        HypixelApiServerChangeEvent(
            packet.serverName,
            packet.serverType.getOrNull(),
            packet.lobbyName.getOrNull(),
            packet.mode.getOrNull(),
            packet.map.getOrNull(),
        ).post()
    }

    fun sendPacket(packet: ServerboundVersionedPacket) {
        try {
            modApi.sendPacket(packet)
        } catch (_: Exception) {
        }
    }
}
