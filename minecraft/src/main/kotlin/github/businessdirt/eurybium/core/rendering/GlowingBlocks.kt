package github.businessdirt.eurybium.core.rendering

import com.google.gson.annotations.Expose
import gg.essential.universal.UMinecraft.getMinecraft
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource

class GlowingBlock(@Expose val position: BlockPos) {
    fun getState(): BlockState = getMinecraft().level?.getBlockState(position) ?: Blocks.AIR.defaultBlockState()

    fun isValidRenderable(): Boolean = !getState().isAir && getState().renderShape == RenderShape.MODEL

    fun getModelParts(): List<BlockStateModelPart> =
        buildList {
            getMinecraft().modelManager.blockStateModelSet.get(getState())
                .collectParts(RandomSource.create(getState().getSeed(position)), this)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GlowingBlock

        return position == other.position
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }
}

@Suppress("unused")
class BatchedGlowingBlockMap {

    private val map: MutableMap<ChromaColour, MutableSet<GlowingBlock>> = mutableMapOf()

    fun add(color: ChromaColour, block: GlowingBlock) {
        map.getOrPut(color) { mutableSetOf() }.add(block)
    }

    fun addAll(color: ChromaColour, blocks: Collection<GlowingBlock>?) {
        if (blocks == null) return
        map.getOrPut(color) { mutableSetOf() }.addAll(blocks)
    }

    fun remove(color: ChromaColour, block: GlowingBlock) {
        map[color]?.remove(block)
    }

    fun remove(color: ChromaColour) : MutableSet<GlowingBlock>? =
        map.remove(color)

    fun clear() = map.clear()

    fun isNotEmpty(): Boolean = map.isNotEmpty()
    fun isEmpty(): Boolean = map.isEmpty()

    fun forEach(function: (ChromaColour, MutableSet<GlowingBlock>) -> Unit) {
        map.forEach { (color, glowingBlocks) ->
            function(color, glowingBlocks)
        }
    }
}