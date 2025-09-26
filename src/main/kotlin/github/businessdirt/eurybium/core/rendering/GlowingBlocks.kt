package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UMinecraft.getMinecraft
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.model.BlockModelPart
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

class GlowingBlock(val position: BlockPos) {
    fun getState(): BlockState = getMinecraft().world?.getBlockState(position) ?: Blocks.AIR.defaultState

    fun isValidRenderable(): Boolean = !getState().isAir && getState().renderType == BlockRenderType.MODEL

    fun getModelParts(): List<BlockModelPart> =
        getMinecraft().blockRenderManager
            .getModel(getState())
            .getParts(Random.create(getState().getRenderingSeed(position)))
}

class BatchedGlowingBlockMap {

    private val map: MutableMap<ChromaColour, MutableSet<GlowingBlock>> = mutableMapOf()

    fun add(color: ChromaColour, block: GlowingBlock) {
        map.getOrPut(color) { mutableSetOf() }.add(block)
    }

    fun addAll(color: ChromaColour, blocks: Collection<GlowingBlock>) {
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