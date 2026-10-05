package github.businessdirt.eurybium.core.events

import gg.essential.universal.UMatrixStack
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext

abstract class RenderingEurybiumEvent(open val context: LevelRenderContext) : EurybiumEvent() {
    val matrixStack = UMatrixStack(context.poseStack()).fork().apply {
        val camera = context.levelState().cameraRenderState.pos
        translate(-camera.x, -camera.y, -camera.z)
    }
}
