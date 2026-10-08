package github.businessdirt.eurybium.api.skyblock

import github.businessdirt.eurybium.data.model.IslandType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiningAPITest {
    @Test
    fun `tunnels exclude ordinary Dwarven locations and other mining islands`() {
        for (line in listOf("⏣ Glacite Tunnels", "⏣ Dwarven Base Camp", "❄ 0 Cold", "Cold: 0")) {
            assertTrue(MiningAPI.isGlaciteTunnels(IslandType.DWARVEN_MINES, listOf(line)))
            for (island in listOf(IslandType.HUB, IslandType.MINESHAFT, IslandType.CRYSTAL_HOLLOWS)) {
                assertFalse(MiningAPI.isGlaciteTunnels(island, listOf(line)))
            }
        }
        assertFalse(MiningAPI.isGlaciteTunnels(IslandType.DWARVEN_MINES, listOf("⏣ Royal Mines")))
        assertFalse(MiningAPI.isGlaciteTunnels(IslandType.DWARVEN_MINES, emptyList()))
    }
}
