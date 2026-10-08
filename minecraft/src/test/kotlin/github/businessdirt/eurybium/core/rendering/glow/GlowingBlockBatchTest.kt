package github.businessdirt.eurybium.core.rendering.glow

import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.core.BlockPos
import kotlin.test.Test
import kotlin.test.assertEquals

class GlowingBlockBatchTest {
    @Test
    fun `exclusive color replaces only the matching position`() {
        val blocks = BatchedGlowingBlockMap()
        val oldColor = ChromaColour.fromRGB(255, 0, 0, 0, 255)
        val newColor = ChromaColour.fromRGB(0, 255, 0, 0, 255)
        val first = GlowingBlock(BlockPos(1, 2, 3))
        val second = GlowingBlock(BlockPos(4, 5, 6))
        blocks.addAll(oldColor, listOf(first, second))
        blocks.addExclusive(newColor, GlowingBlock(first.position))
        blocks.addExclusive(newColor, first)
        val batches = mutableMapOf<ChromaColour, Set<GlowingBlock>>()
        blocks.forEach { color, batch -> batches[color] = batch.toSet() }
        assertEquals(setOf(second), batches[oldColor])
        assertEquals(setOf(first), batches[newColor])
    }
}
