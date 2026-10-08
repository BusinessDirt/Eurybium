package github.businessdirt.eurybium.api.skyblock

import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI
import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationTracker
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import net.hypixel.data.type.GameType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiningAPITest {
    @Test
    fun `tunnels use the detected area and require the Dwarven Mines island`() {
        val tracker = HypixelLocationAPI::class.java.getDeclaredField("tracker")
            .apply { isAccessible = true }.get(HypixelLocationAPI) as HypixelLocationTracker
        val pattern = Regex("""\s*(?<symbol>[⏣ф])\s+(?<area>.+)""")

        try {
            tracker.serverChanged(HypixelApiServerChangeEvent("mini1", GameType.SKYBLOCK, null, "mining_3", null))
            assertFalse(MiningAPI.inGlaciteTunnels)
            for (area in listOf("Glacite Tunnels", "Dwarven Base Camp", "Great Glacite Lake")) {
                tracker.areaUpdated(listOf(" ⏣ $area"), pattern)
                assertTrue(MiningAPI.inGlaciteTunnels, area)
            }
            tracker.areaUpdated(listOf(" ⏣ Royal Mines"), pattern)
            assertFalse(MiningAPI.inGlaciteTunnels)
            tracker.serverChanged(HypixelApiServerChangeEvent("mini2", GameType.SKYBLOCK, null, "hub", null))
            tracker.areaUpdated(listOf(" ⏣ Glacite Tunnels"), pattern)
            assertFalse(MiningAPI.inGlaciteTunnels)
        } finally {
            tracker.disconnected()
        }
    }
}
