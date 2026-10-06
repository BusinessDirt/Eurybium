package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiJoinEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import net.hypixel.data.region.Environment
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import java.util.concurrent.atomic.AtomicLong
import kotlin.jvm.optionals.getOrNull

/**
 * Converts network packets to immutable events and emits them through the client-thread [enqueue].
 *
 * A connection generation invalidates callbacks already queued before a join/disconnect boundary.
 * The bridge relies on the injected executor to preserve packet submission order.
 */
internal class HypixelPacketBridge(
    private val enqueue: (() -> Unit) -> Unit,
    private val emit: (EurybiumEvent) -> Unit,
) {
    private val generation = AtomicLong()

    /** Invalidates pending callbacks while allowing packets submitted afterward to use the new generation. */
    fun invalidateConnection() {
        generation.incrementAndGet()
    }

    /** Maps the hello environment to the mod's production/alpha connection event. */
    fun hello(packet: ClientboundHelloPacket) =
        dispatch(HypixelApiJoinEvent(packet.environment != Environment.PRODUCTION))

    /** Copies optional packet fields before deferring dispatch, avoiding later reads from the packet. */
    fun location(packet: ClientboundLocationPacket) = dispatch(
        HypixelApiServerChangeEvent(
            serverName = packet.serverName,
            serverType = packet.serverType.getOrNull(),
            lobbyName = packet.lobbyName.getOrNull(),
            mode = packet.mode.getOrNull(),
            map = packet.map.getOrNull(),
        )
    )

    private fun dispatch(event: EurybiumEvent) {
        val connection = generation.get()

        enqueue {
            // A disconnect may occur while this callback is waiting in the client executor.
            // Dropping its old generation prevents it from restoring the previous connection's state.
            if (connection == generation.get()) {
                emit(event)
            }
        }
    }
}
