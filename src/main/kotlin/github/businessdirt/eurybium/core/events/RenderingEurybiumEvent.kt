package github.businessdirt.eurybium.core.events

import gg.essential.universal.UMatrixStack
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

abstract class RenderingEurybiumEvent(override val context: WorldRenderContext) : EurybiumEvent(), EurybiumEvent.Rendering {
    val matrixStack: UMatrixStack get() =
        if (context.matrixStack() != null) UMatrixStack(context.matrixStack()!!) else UMatrixStack.Compat.get()
}