package github.businessdirt.eurybium.api.events

import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI
import github.businessdirt.eurybium.data.model.IslandType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class EventListenerFiltersTest {
    class Event : EurybiumEvent()

    @TestFactory
    @DisplayName("SkyBlock single island and island sets follow current state and combine restrictions")
    fun skyBlockSingleIslandAndIslandSetsFollowCurrentStateAndCombineRestrictions(): List<DynamicTest> {
        val names = listOf("inHypixel", "inAlpha", "inSkyBlock", "island")
        val fields = names.associateWith {
            HypixelLocationAPI::class.java.getDeclaredField(it).apply { isAccessible = true }
        }

        val listeners = listOf(
            HandleEvent(),
            HandleEvent(onlyOnSkyblock = true),
            HandleEvent(onlyOnIsland = IslandType.HUB),
            HandleEvent(onlyOnIslands = arrayOf(IslandType.HUB, IslandType.GARDEN)),
            HandleEvent(onlyOnIsland = IslandType.HUB, onlyOnIslands = arrayOf(IslandType.GARDEN)),
        ).map { EurybiumEventListener("test", {}, it) }

        val testCases = listOf(
            State(
                connected = false,
                skyBlock = false,
                island = IslandType.HUB,
                expected = listOf(true, false, false, false, false)
            ),
            State(
                connected = true,
                skyBlock = false,
                island = IslandType.HUB,
                expected = listOf(true, false, false, false, false)
            ),
            State(
                connected = false,
                skyBlock = true,
                island = IslandType.HUB,
                expected = listOf(true, false, false, false, false)
            ),
            State(
                connected = true,
                skyBlock = true,
                island = IslandType.HUB,
                expected = listOf(true, true, true, true, false)
            ),
            State(
                connected = true,
                skyBlock = true,
                island = IslandType.GARDEN,
                expected = listOf(true, true, false, true, false)
            ),
            State(
                connected = true,
                skyBlock = true,
                island = IslandType.THE_END,
                expected = listOf(true, true, false, false, false)
            ),
        )

        return testCases.map { state ->
            val displayName = "connected=${state.connected}, skyBlock=${state.skyBlock}, island=${state.island}"
            dynamicTest(displayName) {
                val original = fields.mapValues { it.value.get(HypixelLocationAPI) }
                try {
                    fields.getValue("inAlpha").set(HypixelLocationAPI, false)
                    fields.getValue("inHypixel").set(HypixelLocationAPI, state.connected)
                    fields.getValue("inSkyBlock").set(HypixelLocationAPI, state.skyBlock)
                    fields.getValue("island").set(HypixelLocationAPI, state.island)

                    assertEquals(state.expected, listeners.map { it.shouldInvoke(Event()) })
                } finally {
                    original.forEach { (name, value) -> fields.getValue(name).set(HypixelLocationAPI, value) }
                }
            }
        }
    }

    private data class State(val connected: Boolean, val skyBlock: Boolean, val island: IslandType, val expected: List<Boolean>)
}
