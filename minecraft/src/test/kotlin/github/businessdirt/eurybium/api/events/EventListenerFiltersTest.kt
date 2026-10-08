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
        val tracker = HypixelLocationAPI::class.java.getDeclaredField("tracker").apply { isAccessible = true }.get(HypixelLocationAPI)
        val stateField = tracker.javaClass.getDeclaredField("state").apply { isAccessible = true }

        val listeners = listOf(
            HandleEvent(),
            HandleEvent(onlyOnSkyBlock = true),
            HandleEvent(onIslands = arrayOf(IslandType.HUB)),
            HandleEvent(onIslands = arrayOf(IslandType.HUB, IslandType.GARDEN)),
        ).map { EurybiumEventListener("test", {}, it) }

        val testCases = listOf(
            State(
                connected = false,
                skyBlock = false,
                island = IslandType.HUB,
                expected = listOf(true, false, false, false)
            ),
            State(
                connected = true,
                skyBlock = false,
                island = IslandType.HUB,
                expected = listOf(true, false, false, false)
            ),
            State(
                connected = false,
                skyBlock = true,
                island = IslandType.HUB,
                expected = listOf(true, false, false, false)
            ),
            State(
                connected = true,
                skyBlock = true,
                island = IslandType.HUB,
                expected = listOf(true, true, true, true)
            ),
            State(
                connected = true,
                skyBlock = true,
                island = IslandType.GARDEN,
                expected = listOf(true, true, false, true)
            ),
            State(
                connected = true,
                skyBlock = true,
                island = IslandType.THE_END,
                expected = listOf(true, true, false, false)
            ),
        )

        return testCases.map { state ->
            val displayName = "connected=${state.connected}, skyBlock=${state.skyBlock}, island=${state.island}"
            dynamicTest(displayName) {
                val original = stateField.get(tracker)
                try {
                    stateField.set(tracker, github.businessdirt.eurybium.api.hypixelapi.HypixelLocationState(
                        inHypixel = state.connected,
                        serverType = if (state.skyBlock) net.hypixel.data.type.GameType.SKYBLOCK else null,
                        island = state.island,
                    ))
                    assertEquals(state.expected, listeners.map { it.shouldInvoke(Event()) })
                } finally {
                    stateField.set(tracker, original)
                }
            }
        }
    }

    private data class State(val connected: Boolean, val skyBlock: Boolean, val island: IslandType, val expected: List<Boolean>)
}
