package github.businessdirt.eurybium.core.rendering

import com.google.gson.annotations.Expose
import gg.essential.universal.UMinecraft.getMinecraft
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.model.BlockModelPart
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

class GlowingBlock(@Expose val position: BlockPos) {
    fun getState(): BlockState = getMinecraft().world?.getBlockState(position) ?: Blocks.AIR.defaultState

    fun isValidRenderable(): Boolean = !getState().isAir && getState().renderType == BlockRenderType.MODEL

    fun getModelParts(): List<BlockModelPart> =
        getMinecraft().blockRenderManager
            .getModel(getState())
            .getParts(Random.create(getState().getRenderingSeed(position)))

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