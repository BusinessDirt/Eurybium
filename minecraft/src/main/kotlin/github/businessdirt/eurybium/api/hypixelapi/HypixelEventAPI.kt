package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.core.scheduling.ClientTasks
import github.businessdirt.eurybium.core.scheduling.ScheduledTask
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.ClientJoinEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundVersionedPacket

/**
 * Bridges Hypixel Mod API packets to client-thread mod events and schedules outgoing packets.
 *
 * Handlers are installed once when this module initializes. Connection boundaries invalidate
 * pending incoming callbacks before other handlers reset or consume location state.
 */
@EurybiumModule
object HypixelEventAPI {
    private val modApi = HypixelModAPI.getInstance()
    private val bridge = HypixelPacketBridge(
        enqueue = { ClientTasks.runOrNextTick("Hypixel packet event", it) },
        emit = { it.post() },
    )

    init {
        modApi.createHandler(ClientboundHelloPacket::class.java, bridge::hello)
        modApi.createHandler(ClientboundLocationPacket::class.java, bridge::location)

        // Install receivers before subscribing so an immediate location response has a handler.
        modApi.subscribeToEventPacket(ClientboundLocationPacket::class.java)
    }

    @HandleEvent(eventTypes = [ClientJoinEvent::class, ClientDisconnectEvent::class], priority = Int.MIN_VALUE)
    private fun onConnectionChanged() = bridge.invalidateConnection()

    /**
     * Sends [packet] on the client thread and returns a cancellable result handle.
     *
     * A successful `false` result means the transport declined the packet. Exceptions are
     * logged and retained as failures in [ScheduledTask.result], rather than silently ignored.
     * The packet must not be mutated after submission when sending is deferred.
     */
    fun sendPacket(packet: ServerboundVersionedPacket): ScheduledTask<Boolean> =
        ClientTasks.runOrNextTickReturning("Send Hypixel packet ${packet.javaClass.simpleName}") {
            modApi.sendPacket(packet)
        }
}
