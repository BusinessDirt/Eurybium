package github.businessdirt.eurybium.core.rendering.glow

import kotlinx.coroutines.runBlocking
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MiningNodeScannerTest {
    private val diamond = "minecraft:diamond_ore"
    private val glass = "minecraft:magenta_stained_glass"

    private fun scan(
        blocks: Map<BlockPos, String>,
        range: Double = 3.0,
        preference: String? = null,
        unloaded: Set<BlockPos> = emptySet(),
    ): ScannedMiningNode? = runBlocking {
        scanMiningNode(BlockPos.ZERO, range, preference, { pos ->
            if (pos in unloaded) null else blocks[pos] ?: "minecraft:air"
        }, {})
    }

    @Test
    fun `closest eligible material wins and traversal extends beyond search range`() {
        val positions = (1..6).associate { BlockPos(it, 0, 0) to glass } +
            (1..2).associate { BlockPos(0, it + 1, 0) to diamond }
        val node = scan(positions, range = 1.0)!!
        assertEquals(glass, node.material)
        assertEquals((1..6).map { BlockPos(it, 0, 0) }.toSet(), node.positions.toSet())
        assertEquals(Vec3(4.0, 0.5, 0.5), node.center)
    }

    @Test
    fun `preference ignores nearer gems and includes deepslate ore`() {
        val positions = mapOf(
            BlockPos(1, 0, 0) to glass,
            BlockPos(0, 2, 0) to diamond,
            BlockPos(0, 3, 0) to "minecraft:deepslate_diamond_ore",
            BlockPos(1, 3, 0) to "minecraft:gold_ore",
        )
        val node = scan(positions, preference = diamond)!!
        assertEquals(setOf(BlockPos(0, 2, 0), BlockPos(0, 3, 0)), node.positions.toSet())
        assertNull(scan(positions, preference = "minecraft:emerald_ore"))
        assertNull(scan(positions, preference = "invalid"))
    }

    @Test
    fun `same color panes join glass but diagonal blocks and other colors do not`() {
        val positions = mapOf(
            BlockPos.ZERO to glass,
            BlockPos(1, 0, 0) to "minecraft:magenta_stained_glass_pane",
            BlockPos(1, 1, 1) to glass,
            BlockPos(2, 0, 0) to "minecraft:red_stained_glass",
        )
        assertEquals(setOf(BlockPos.ZERO, BlockPos(1, 0, 0)), scan(positions, range = 0.0)!!.positions.toSet())
    }

    @Test
    fun `unloaded search or cluster boundary never returns partial geometry`() {
        val positions = mapOf(BlockPos.ZERO to diamond)
        assertNull(scan(positions, unloaded = setOf(BlockPos(1, 0, 0))))
        assertNull(scan(positions, range = 0.0, unloaded = setOf(BlockPos(1, 0, 0))))
    }

    @Test
    fun `oversized clusters fall back rather than render a truncated component`() {
        val positions = (0..MAX_MINING_NODE_BLOCKS).associate { BlockPos(it, 0, 0) to diamond }
        assertNull(scan(positions, range = 0.0))
        assertEquals(MAX_MINING_NODE_BLOCKS, scan(positions - BlockPos(MAX_MINING_NODE_BLOCKS, 0, 0), range = 0.0)!!.positions.size)
    }

    @Test
    fun `waypoint on an ore skips even the largest radius search`() = runBlocking {
        var reads = 0
        val node = scanMiningNode(BlockPos.ZERO, 32.0, null, {
            reads++
            if (it == BlockPos.ZERO) diamond else "minecraft:air"
        }, {})
        assertEquals(1, node!!.positions.size)
        assertEquals(8, reads) // Origin, seed validation, and the six adjacent faces.
    }

    @Test
    fun `every world read has a cooperative checkpoint and invalid ranges do no work`() = runBlocking {
        var checkpoints = 0
        var reads = 0
        val node = scanMiningNode(BlockPos.ZERO, 0.0, null, {
            reads++
            assertEquals(reads, checkpoints)
            if (it == BlockPos.ZERO) diamond else "minecraft:air"
        }, { checkpoints++ })
        assertEquals(1, node!!.positions.size)
        assertTrue(reads > 1)
        for (range in listOf(Double.NaN, -1.0, 33.0, Double.POSITIVE_INFINITY)) {
            assertNull(scanMiningNode(BlockPos.ZERO, range, null, { error("Unexpected world read") }, {}))
        }
    }
}
