package github.businessdirt.eurybium.core.rendering.glow

import github.businessdirt.eurybium.config.features.mining.WaypointNodeGlowConfig
import github.businessdirt.eurybium.data.model.MiningNodeMaterial
import kotlinx.coroutines.runBlocking
import net.minecraft.core.BlockPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MiningNodePolicyTest {
    @Test
    fun `region switches exclusions and immutable policy snapshots are independent`() {
        val config = WaypointNodeGlowConfig()
        assertNull(config.policy(inMineshaft = true))
        config.expandNodes = true
        config.glaciteTunnels.enabled = true
        val original = config.policy(inGlaciteTunnels = true)!!
        config.glaciteTunnels.excludedMaterials += MiningNodeMaterial.MITHRIL
        assertFalse(MiningNodeMaterial.MITHRIL in config.policy(inGlaciteTunnels = true)!!.allowedMaterials)
        assertTrue(MiningNodeMaterial.MITHRIL in original.allowedMaterials)
        assertTrue(MiningNodeMaterial.MITHRIL in config.policy(inCrystalHollows = true)!!.allowedMaterials)
        config.mineshafts.enabled = false
        assertNull(config.policy(inMineshaft = true))
        assertNull(config.policy())
    }

    @Test
    fun `resource variants group correctly without treating Hollows scenery as tunnel ores`() {
        for (id in listOf("minecraft:gray_wool", "minecraft:cyan_terracotta", "minecraft:prismarine", "minecraft:light_blue_wool")) {
            assertEquals(MiningNodeMaterial.MITHRIL, MiningNodeMaterial.forBlock(id, dwarvenMaterials = true))
        }
        assertNull(MiningNodeMaterial.forBlock("minecraft:gray_wool", inCrystalHollows = true))
        assertNull(MiningNodeMaterial.forBlock("minecraft:cobblestone", inCrystalHollows = true))
        assertEquals(MiningNodeMaterial.TUNGSTEN, MiningNodeMaterial.forBlock("minecraft:clay", dwarvenMaterials = true))
        assertEquals(MiningNodeMaterial.GOLD, MiningNodeMaterial.forBlock("minecraft:gold_block", inCrystalHollows = true))
        assertEquals(MiningNodeMaterial.JASPER, MiningNodeMaterial.fromId("JASPER"))
        assertEquals(MiningNodeMaterial.JASPER, MiningNodeMaterial.fromId("minecraft:magenta_stained_glass_pane"))
    }

    @Test
    fun `excluded nearest resource yields to the next allowed cluster`() = runBlocking {
        val blocks = mapOf(BlockPos.ZERO to "minecraft:prismarine", BlockPos(2, 0, 0) to "minecraft:magenta_stained_glass")
        val node = scanMiningNode(BlockPos.ZERO, 3.0, null,
            { blocks[it] ?: "minecraft:air" }, {},
            MiningNodeMaterial.entries.toSet() - MiningNodeMaterial.MITHRIL,
            dwarvenMaterials = true,
        )!!
        assertEquals(MiningNodeMaterial.JASPER.id, node.material)
        assertEquals(listOf(BlockPos(2, 0, 0)), node.positions)
    }

    @Test
    fun `excluded explicit preference never silently switches to another material`() = runBlocking {
        assertNull(scanMiningNode(BlockPos.ZERO, 3.0, "mithril",
            { error("An excluded preference should not scan") }, {},
            MiningNodeMaterial.entries.toSet() - MiningNodeMaterial.MITHRIL,
            dwarvenMaterials = true,
        ))
    }
}
