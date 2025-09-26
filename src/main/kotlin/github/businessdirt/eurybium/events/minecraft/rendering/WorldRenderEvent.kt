package github.businessdirt.eurybium.events.minecraft.rendering

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.core.events.RenderingEurybiumEvent
import github.businessdirt.eurybium.core.rendering.BoxRenderer
import github.businessdirt.eurybium.core.rendering.CameraTransform
import github.businessdirt.eurybium.core.rendering.LineRenderer
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class WorldRenderEvent(override val context: WorldRenderContext) : RenderingEurybiumEvent(context) {

    init {
        CameraTransform.offset(matrixStack)
    }

    fun draw3DLine(p1: Vec3d, p2: Vec3d, color: ChromaColour, lineWidth: Int, depth: Boolean) {
        matrixStack.push()
        LineRenderer.draw3DLine(matrixStack, p1, p2, color.getEffectiveColour(), lineWidth.toFloat(), depth)
        matrixStack.pop()
    }

    fun drawLineToEye(location: Vec3d, color: ChromaColour, lineWidth: Int, depth: Boolean) {
        val tickProgress = context.tickCounter().getTickProgress(false)
        val player = getMinecraft().player ?: return
        draw3DLine(
            player.getCameraPosVec(tickProgress).add(player.getRotationVec(tickProgress)),
            location,
            color,
            lineWidth,
            depth
        )
    }

    fun drawWaypointFilled(
        location: BlockPos,
        color: ChromaColour,
        depth: Boolean = true
    ) {
        matrixStack.push()
        BoxRenderer.drawFilledBoundingBox(matrixStack, location, color, depth)
        matrixStack.pop()
    }

    fun drawWaypointOutlined(location: BlockPos, color: ChromaColour, lineWidth: Int, depth: Boolean) {
        matrixStack.push()
        BoxRenderer.drawOutlinedBoundingBox(matrixStack, location, color, lineWidth.toFloat(), depth)
        matrixStack.pop()
    }
}