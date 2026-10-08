package github.businessdirt.eurybium.core.rendering.glow

import github.businessdirt.eurybium.config.features.mining.WaypointNodeGlowConfig
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.MiningNodeMaterial
import github.businessdirt.eurybium.data.model.MiningNodeRegion
import kotlinx.coroutines.runBlocking
import net.minecraft.core.BlockPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MiningNodePolicyTest {
    @Test
    fun `regions distinguish tunnels from ordinary Dwarven locations`() {
        assertEquals(MiningNodeRegion.MINESHAFT, MiningNodeRegion.resolve(IslandType.MINESHAFT, emptyList()))
        assertEquals(MiningNodeRegion.CRYSTAL_HOLLOWS, MiningNodeRegion.resolve(IslandType.CRYSTAL_HOLLOWS, emptyList()))
        for (line in listOf("⏣ Glacite Tunnels", "⏣ Dwarven Base Camp", "❄ 0 Cold", "Cold: 0")) {
            assertEquals(MiningNodeRegion.GLACITE_TUNNELS, MiningNodeRegion.resolve(IslandType.DWARVEN_MINES, listOf(line)))
            assertNull(MiningNodeRegion.resolve(IslandType.HUB, listOf(line)))
        }
        assertNull(MiningNodeRegion.resolve(IslandType.DWARVEN_MINES, listOf("⏣ Royal Mines")))
    }

    @Test
    fun `region switches exclusions and immutable policy snapshots are independent`() {
        val config = WaypointNodeGlowConfig()
        assertNull(config.policy(MiningNodeRegion.MINESHAFT))
        config.expandNodes = true
        val original = config.policy(MiningNodeRegion.GLACITE_TUNNELS)!!
        config.glaciteTunnels.excludedMaterials += MiningNodeMaterial.MITHRIL
        assertFalse(MiningNodeMaterial.MITHRIL in config.policy(MiningNodeRegion.GLACITE_TUNNELS)!!.allowedMaterials)
        assertTrue(MiningNodeMaterial.MITHRIL in original.allowedMaterials)
        assertTrue(MiningNodeMaterial.MITHRIL in config.policy(MiningNodeRegion.CRYSTAL_HOLLOWS)!!.allowedMaterials)
        config.mineshafts.enabled = false
        assertNull(config.policy(MiningNodeRegion.MINESHAFT))
        assertNull(config.policy(null))
    }

    @Test
    fun `resource variants group correctly without treating Hollows scenery as tunnel ores`() {
        for (id in listOf("minecraft:gray_wool", "minecraft:cyan_terracotta", "minecraft:prismarine", "minecraft:light_blue_wool")) {
            assertEquals(MiningNodeMaterial.MITHRIL, MiningNodeMaterial.forBlock(id, MiningNodeRegion.GLACITE_TUNNELS))
        }
        assertNull(MiningNodeMaterial.forBlock("minecraft:gray_wool", MiningNodeRegion.CRYSTAL_HOLLOWS))
        assertNull(MiningNodeMaterial.forBlock("minecraft:cobblestone", MiningNodeRegion.CRYSTAL_HOLLOWS))
        assertEquals(MiningNodeMaterial.TUNGSTEN, MiningNodeMaterial.forBlock("minecraft:clay", MiningNodeRegion.MINESHAFT))
        assertEquals(MiningNodeMaterial.GOLD, MiningNodeMaterial.forBlock("minecraft:gold_block", MiningNodeRegion.CRYSTAL_HOLLOWS))
        assertEquals(MiningNodeMaterial.JASPER, MiningNodeMaterial.fromId("JASPER"))
        assertEquals(MiningNodeMaterial.JASPER, MiningNodeMaterial.fromId("minecraft:magenta_stained_glass_pane"))
    }

    @Test
    fun `excluded nearest resource yields to the next allowed cluster`() = runBlocking {
        val blocks = mapOf(BlockPos.ZERO to "minecraft:prismarine", BlockPos(2, 0, 0) to "minecraft:magenta_stained_glass")
        val node = scanMiningNode(BlockPos.ZERO, 3.0, null,
            { blocks[it] ?: "minecraft:air" }, {},
            MiningNodeMaterial.entries.toSet() - MiningNodeMaterial.MITHRIL,
            MiningNodeRegion.GLACITE_TUNNELS,
        )!!
        assertEquals(MiningNodeMaterial.JASPER.id, node.material)
        assertEquals(listOf(BlockPos(2, 0, 0)), node.positions)
    }

    @Test
    fun `excluded explicit preference never silently switches to another material`() = runBlocking {
        assertNull(scanMiningNode(BlockPos.ZERO, 3.0, "mithril",
            { error("An excluded preference should not scan") }, {},
            MiningNodeMaterial.entries.toSet() - MiningNodeMaterial.MITHRIL,
            MiningNodeRegion.GLACITE_TUNNELS,
        ))
    }
}
