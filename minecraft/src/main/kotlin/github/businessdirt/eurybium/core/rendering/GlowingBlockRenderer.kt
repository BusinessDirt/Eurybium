package github.businessdirt.eurybium.core.rendering

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.events.PreModInitializationEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier

@EurybiumModule
object GlowingBlockRenderer {

    val blocks = BatchedGlowingBlockMap()

    private data class PreparedBlock(val position: BlockPos, val parts: List<BlockStateModelPart>, val color: Int)
    private var prepared: List<PreparedBlock> = emptyList()
    internal val profile = GlowingBlockRenderProfile()

    @HandleEvent(eventType = PreModInitializationEvent::class)
    private fun onPreModInitializationEvent() {
        // Requests made during the previous drawing phase are prepared without retaining live world objects.
        LevelRenderEvents.END_EXTRACTION.register { context ->
            GlowingBlockRendererTestCommands.queueTestBlocks()
            val start = System.nanoTime()
            prepared = buildList {
                blocks.forEach { color, group ->
                    val rgb = color.getEffectiveColour().rgb
                    group.filter { it.isValidRenderable() }.forEach { add(PreparedBlock(it.position, it.getModelParts(), rgb)) }
                }
            }
            blocks.clear()
            if (prepared.isNotEmpty()) context.levelState().haveGlowingEntities = true
            profile.recordPreparation(System.nanoTime() - start, prepared.size, prepared.sumOf { it.parts.size })
        }

        LevelRenderEvents.COLLECT_SUBMITS.register { context ->
            val start = System.nanoTime()
            val camera = context.levelState().cameraRenderState.pos
            val matrices = context.poseStack()
            val layer = RenderTypes.outline(Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"))
            prepared.forEach { block ->
                matrices.pushPose()
                try {
                    matrices.translate(block.position.x - camera.x, block.position.y - camera.y, block.position.z - camera.z)
                    context.submitNodeCollector().submitBlockModel(matrices, layer, block.parts, intArrayOf(-1), 0xF000F0, OverlayTexture.NO_OVERLAY, block.color)
                } finally { matrices.popPose() }
            }
            prepared = emptyList()
            profile.recordSubmission(System.nanoTime() - start)
        }
    }

    @HandleEvent(eventTypes = [ WorldChangeEvent::class, ClientDisconnectEvent::class ])
    private fun onWorldClearEvents() { blocks.clear(); prepared = emptyList() }

}
