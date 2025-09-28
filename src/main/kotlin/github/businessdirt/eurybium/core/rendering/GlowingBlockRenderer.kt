package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.modules.EurybiumModule
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderAfterEntitiesEvent
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

@EurybiumModule
object GlowingBlockRenderer {

    private var renderLayer: RenderLayer? = null
    val blocks: BatchedGlowingBlockMap = BatchedGlowingBlockMap()

    fun shouldRender() = blocks.isNotEmpty()

    fun render(matrixStack: MatrixStack, camera: Camera) {
        if (blocks.isEmpty()) return

        val world = getMinecraft().world ?: return
        val outlineProvider = getMinecraft().bufferBuilders.outlineVertexConsumers

        val layer = renderLayer ?: RenderLayer.getItemEntityTranslucentCull(
            Identifier.of("minecraft", "textures/atlas/blocks.png")
        ).also { renderLayer = it }

        blocks.forEach { color, glowingBlocks ->
            if (glowingBlocks.isEmpty()) return@forEach
            setColor(outlineProvider, color)

            glowingBlocks.forEach { block ->
                if (block.isValidRenderable()) {
                    matrixStack.push()
                    matrixStack.translate(
                        block.position.x - camera.pos.x,
                        block.position.y - camera.pos.y,
                        block.position.z - camera.pos.z
                    )

                    getMinecraft().blockRenderManager.modelRenderer.render(
                        world,
                        block.getModelParts(),
                        block.getState(),
                        block.position,
                        matrixStack,
                        InvisibleVertexConsumer(outlineProvider.getBuffer(layer)),
                        false,
                        OverlayTexture.DEFAULT_UV
                    )

                    matrixStack.pop()
                }
            }
        }

        blocks.clear()
        outlineProvider.draw()
    }

    private fun setColor(outlineProvider: OutlineVertexConsumerProvider, color: ChromaColour) {
        val color = color.getEffectiveColour()
        outlineProvider.setColor(color.red, color.green, color.blue, color.alpha)
    }

    @HandleEvent
    fun onWorldChangeEvent(event: WorldChangeEvent) {
        blocks.clear()
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