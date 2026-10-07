package github.businessdirt.eurybium.events.minecraft.rendering

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.api.events.RenderingEurybiumEvent
import github.businessdirt.eurybium.core.rendering.BoxRenderer
import github.businessdirt.eurybium.core.rendering.LineRenderer
import github.businessdirt.eurybium.core.rendering.glow.GlowingBlock
import github.businessdirt.eurybium.core.rendering.glow.GlowingBlockRenderer
import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.world.phys.Vec3

class WorldRenderLastEvent(context: LevelRenderContext) : RenderingEurybiumEvent(context) {

    fun draw3DLine(p1: Vec3, p2: Vec3, color: ChromaColour, lineWidth: Int, depth: Boolean) {
        matrixStack.push()
        LineRenderer.draw3DLine(matrixStack, p1, p2, color.getEffectiveColour(), lineWidth.toFloat(), depth)
        matrixStack.pop()
    }

    fun drawLineToEye(location: Vec3, color: ChromaColour, lineWidth: Int, depth: Boolean) {
        val tickProgress = getMinecraft().deltaTracker.getGameTimeDeltaPartialTick(false)
        val player = getMinecraft().player ?: return
        draw3DLine(
            player.getEyePosition(tickProgress).add(player.getViewVector(tickProgress)),
            location,
            color,
            lineWidth,
            depth
        )
    }

    fun drawWaypointFilled(
        waypoint: EurybiumWaypoint,
        color: ChromaColour,
        depth: Boolean = true,
    ) {
        matrixStack.push()
        BoxRenderer.drawFilledBoundingBox(matrixStack, waypoint.location, color, depth)
        matrixStack.pop()
    }

    fun drawWaypointOutlined(
        waypoint: EurybiumWaypoint,
        color: ChromaColour,
        lineWidth: Int,
        depth: Boolean,
    ) {
        matrixStack.push()
        BoxRenderer.drawOutlinedBoundingBox(matrixStack, waypoint.location, color, lineWidth.toFloat(), depth)
        matrixStack.pop()
    }

    fun drawWaypointGlowing(
        waypoint: EurybiumWaypoint,
        color: ChromaColour,
    ) {
        /**
        if (mineshaftType == MineshaftType.UNKNOWN ||
            GlowingBlockRenderer.gemstoneNodes.mineshaftNodes?.get(mineshaftType.typeIndex)?.isEmpty() == true
        ) {
            GlowingBlockRenderer.blocks.add(color, GlowingBlock(waypoint.location))
            return
        }

        val gemstoneNode = waypoint.getNearestNode(mineshaftType) ?: return
        GlowingBlockRenderer.blocks.addAll(color, gemstoneNode.blocks)
        **/

        val block = GlowingBlock(waypoint.location)
        GlowingBlockRenderer.blocks.add(color, block)
    }
}
