package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.rendering.RenderState.with
import github.businessdirt.eurybium.core.scanner.BlockScanner
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.utils.concurrent.Coroutine
import net.minecraft.block.BlockRenderType
import net.minecraft.block.Blocks
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.random.Random

object BlockOutlineRenderer {

    private var renderLayer: RenderLayer? = null
    private val blocksByType: MutableMap<Identifier, MutableSet<BlockPos>> = mutableMapOf()

    fun shouldRender() = blocksByType.isNotEmpty()

    fun renderBlockOutline(blockPos: BlockPos, identifier: Identifier? = null) {
        val world = getMinecraft().world ?: return

        val id = if (identifier == null) {
            val state = world.getBlockState(blockPos)
            Registries.BLOCK.getId(state.block)
        } else identifier

        blocksByType.getOrPut(id) { mutableSetOf() }.add(blockPos)
    }

    fun renderBlockOutlines(blocks: Map<Identifier, Set<BlockPos>>) {
        blocks.forEach { (id, blockPositions) ->
            blocksByType.getOrPut(id) { mutableSetOf() }.addAll(blockPositions)
        }
    }

    /**
     * Clears all block outlines or just for the specified block
     */
    fun clearBlockOutlines(identifier: Identifier? = null) {
        if (identifier == null) {
            blocksByType.clear()
            return
        }

        blocksByType[identifier]?.clear()
    }

    fun render(matrixStack: MatrixStack, camera: Camera) {
        if (blocksByType.isEmpty()) return

        val outlineProvider = getMinecraft().bufferBuilders.outlineVertexConsumers
        val world = getMinecraft().world ?: return

        if (renderLayer == null) {
            // Needs to be an entity layer for outline rendering to work
            // This one works with glass blocks as well
            renderLayer = RenderLayer.getItemEntityTranslucentCull(Identifier.of("minecraft", "textures/atlas/blocks.png"))
        }

        blocksByType.forEach { (blockId: Identifier, blockPositions: Set<BlockPos>) ->
            if (blockPositions.isNotEmpty()) {
                setColor(outlineProvider, blockId);

                blockPositions.forEach { blockPosition: BlockPos ->
                    val state = world.getBlockState(blockPosition)
                    if (!state.isAir && state.renderType == BlockRenderType.MODEL) {
                        matrixStack.with {
                            matrixStack.translate(blockPosition.x - camera.pos.x, blockPosition.y - camera.pos.y, blockPosition.z - camera.pos.z)

                            val modelParts = getMinecraft().blockRenderManager.getModel(state).getParts(Random.create(state.getRenderingSeed(blockPosition)))
                            val vertexConsumer: VertexConsumer = outlineProvider.getBuffer(renderLayer)
                            val invisibleConsumer = InvisibleVertexConsumer(vertexConsumer)

                            getMinecraft().blockRenderManager.modelRenderer.render(world, modelParts, state, blockPosition, matrixStack,
                                invisibleConsumer, false, OverlayTexture.DEFAULT_UV)
                        }
                    }
                }
            }
        }

        outlineProvider.draw()
    }

    private fun setColor(outlineProvider: OutlineVertexConsumerProvider, blockId: Identifier) {
        var newBlockId: Identifier = blockId
        if (blockId.path.contains("_pane")) newBlockId = Identifier.of(
            blockId.namespace,
            blockId.path.replace("_pane", "")
        )

        val color = Registries.BLOCK.get(newBlockId).defaultMapColor.color
        outlineProvider.setColor(
            ColorHelper.getRed(color),
            ColorHelper.getGreen(color),
            ColorHelper.getBlue(color),
            ColorHelper.getAlpha(color)
        )
    }

    internal class InvisibleVertexConsumer(val delegate: VertexConsumer) : VertexConsumer {
        override fun vertex(
            x: Float,
            y: Float,
            z: Float,
        ): VertexConsumer = this.delegate.vertex(x, y, z)

        override fun color(
            red: Int,
            green: Int,
            blue: Int,
            alpha: Int,
        ): VertexConsumer = this.delegate.color(red, green, blue, 0)

        override fun texture(u: Float, v: Float): VertexConsumer = this.delegate.texture(u, v)

        override fun overlay(u: Int, v: Int): VertexConsumer = this.delegate.overlay(u, v)

        override fun light(u: Int, v: Int): VertexConsumer = this.delegate.light(u, v)

        override fun normal(
            x: Float,
            y: Float,
            z: Float,
        ): VertexConsumer = this.delegate.normal(x, y, z)
    }
}