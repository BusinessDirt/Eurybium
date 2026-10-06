package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiJoinEvent
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import net.hypixel.data.region.Environment
import net.hypixel.data.type.GameType
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import kotlin.test.*

class HypixelPacketBridgeTest {
    private val queued = ArrayDeque<() -> Unit>()
    private val events = mutableListOf<EurybiumEvent>()
    private val bridge = HypixelPacketBridge({ queued.addLast(it) }, events::add)
    private fun flush() {
        while (queued.isNotEmpty()) {
            queued.removeFirst().invoke()
        }
    }

    @Test
    fun `hello and location packets emit in submission order only through the client executor`() {
        bridge.hello(ClientboundHelloPacket(Environment.PRODUCTION))
        bridge.location(ClientboundLocationPacket("mini1", GameType.SKYBLOCK, null, "hub", "map"))
        assertTrue(events.isEmpty())
        flush()
        assertFalse((events[0] as HypixelApiJoinEvent).alpha)
        val location = events[1] as HypixelApiServerChangeEvent
        assertEquals("mini1", location.serverName)
        assertEquals(GameType.SKYBLOCK, location.serverType)
        assertNull(location.lobbyName)
        assertEquals("hub", location.mode)
        assertEquals("map", location.map)
    }

    @Test
    fun `disconnect invalidates queued callbacks and the next connection remains usable`() {
        bridge.hello(ClientboundHelloPacket(Environment.PRODUCTION))
        bridge.location(ClientboundLocationPacket("old", null, null, null, null))
        bridge.invalidateConnection()
        bridge.location(ClientboundLocationPacket("new", null, null, null, null))
        flush()
        val location = assertIs<HypixelApiServerChangeEvent>(events.single())
        assertEquals("new", location.serverName)
        assertNull(location.mode)
        assertNull(location.serverType)
        assertNull(location.map)
    }

    @Test
    fun `all non production hello environments are classified as alpha`() {
        for (environment in Environment.entries.filter { it != Environment.PRODUCTION }) {
            bridge.hello(ClientboundHelloPacket(environment))
        }
        flush()
        assertTrue(events.isNotEmpty())
        assertTrue(events.all { (it as HypixelApiJoinEvent).alpha })
    }
}
