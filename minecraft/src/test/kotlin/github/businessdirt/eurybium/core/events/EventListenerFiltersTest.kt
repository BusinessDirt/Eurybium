package github.businessdirt.eurybium.core.events

import github.businessdirt.eurybium.core.data.HypixelData
import github.businessdirt.eurybium.core.data.model.IslandType
import java.util.function.Consumer
import kotlin.test.Test
import kotlin.test.assertEquals

class EventListenerFiltersTest {
    class Event : EurybiumEvent()

    @Test
    fun `skyblock single island and island sets follow current state and combine restrictions`() {
        val names = listOf("hypixelLive", "hypixelAlpha", "skyBlock", "skyBlockIsland")
        val fields = names.associateWith { HypixelData::class.java.getDeclaredField(it).apply { isAccessible = true } }
        val original = fields.mapValues { it.value.get(HypixelData) }
        try {
            fields.getValue("hypixelAlpha").set(HypixelData, false)
            val listeners = listOf(
                HandleEvent(), HandleEvent(onlyOnSkyblock = true), HandleEvent(onlyOnIsland = IslandType.HUB),
                HandleEvent(onlyOnIslands = arrayOf(IslandType.HUB, IslandType.GARDEN)),
                HandleEvent(onlyOnIsland = IslandType.HUB, onlyOnIslands = arrayOf(IslandType.GARDEN)),
            ).map { EurybiumEventListener("test", Consumer {}, it) }
            for ((connected, skyblock, island, expected) in listOf(
                State(false, false, IslandType.HUB, listOf(true, false, false, false, false)),
                State(true, false, IslandType.HUB, listOf(true, false, false, false, false)),
                State(false, true, IslandType.HUB, listOf(true, false, false, false, false)),
                State(true, true, IslandType.HUB, listOf(true, true, true, true, false)),
                State(true, true, IslandType.GARDEN, listOf(true, true, false, true, false)),
                State(true, true, IslandType.THE_END, listOf(true, true, false, false, false)),
            )) {
                fields.getValue("hypixelLive").set(HypixelData, connected)
                fields.getValue("skyBlock").set(HypixelData, skyblock)
                fields.getValue("skyBlockIsland").set(HypixelData, island)
                assertEquals(expected, listeners.map { it.shouldInvoke(Event()) })
            }
        } finally {
            original.forEach { (name, value) -> fields.getValue(name).set(HypixelData, value) }
        }
    }

    private data class State(val connected: Boolean, val skyblock: Boolean, val island: IslandType, val expected: List<Boolean>)
}
