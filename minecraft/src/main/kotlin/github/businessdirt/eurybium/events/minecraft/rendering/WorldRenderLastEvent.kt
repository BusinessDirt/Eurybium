package github.businessdirt.eurybium.events.minecraft.rendering

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.RenderingEurybiumEvent
import github.businessdirt.eurybium.api.minecraft.math.MatrixExtensions.use
import github.businessdirt.eurybium.core.rendering.BoxRenderer
import github.businessdirt.eurybium.core.rendering.LineRenderer
import github.businessdirt.eurybium.core.rendering.glow.DynamicMiningNodes
import github.businessdirt.eurybium.core.rendering.glow.GlowingBlock
import github.businessdirt.eurybium.core.rendering.glow.GlowingBlockRenderer
import github.businessdirt.eurybium.core.rendering.glow.ScannedMiningNode
import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f

class WorldRenderLastEvent(context: LevelRenderContext) : RenderingEurybiumEvent(context) {

    private val glowPriorities = mutableMapOf<BlockPos, Int>()
    private val expandedPositions = mutableSetOf<BlockPos>()
    private val glowNodes = mutableMapOf<EurybiumWaypoint, ScannedMiningNode?>()

    fun drawLineToEye(location: Vec3, color: ChromaColour, lineWidth: Float, depth: Boolean) = matrixStack.use {
        val camera = context.levelState().cameraRenderState
        val forward = camera.orientation.transform(Vector3f(0f, 0f, -1f))
        val start = camera.pos.add(forward.x.toDouble(), forward.y.toDouble(), forward.z.toDouble())
        val window = getMinecraft().window
        LineRenderer.drawScreenLine(
            matrixStack, start, location, camera.pos,
            Matrix4f(camera.projectionMatrix).mul(camera.viewRotationMatrix),
            window.width, window.height,
            color.getEffectiveColour(), lineWidth, depth,
        )
    }

    fun drawWaypointFilled(
        waypoint: EurybiumWaypoint,
        color: ChromaColour,
        depth: Boolean = true,
    ) = matrixStack.use { BoxRenderer.drawFilledBoundingBox(matrixStack, waypoint.location, color, depth) }

    fun drawWaypointOutlined(
        waypoint: EurybiumWaypoint,
        color: ChromaColour,
        lineWidth: Int,
        depth: Boolean,
    ) = matrixStack.use { BoxRenderer.drawOutlinedBoundingBox(matrixStack, waypoint.location, color, lineWidth.toFloat(), depth) }

    /**
     * Resolves and reserves this frame's glow geometry once. Navigation and rendering share the same
     * center, including the single-block fallback for pending scans or an exhausted block budget.
     */
    fun waypointGlowTarget(waypoint: EurybiumWaypoint): Vec3 = glowNode(waypoint)?.center ?: waypoint.location.center

    private fun glowNode(waypoint: EurybiumWaypoint): ScannedMiningNode? {
        if (glowNodes.containsKey(waypoint)) return glowNodes[waypoint]

        val config = EurybiumMod.config.mining.waypointNodes
        val node = DynamicMiningNodes.request(waypoint.location, config.matchRange.toDouble(), waypoint.nodeMaterial)
        // Reserve entire nodes, never partially expand one because another request used the budget.
        val exceedsBudget = node != null && expandedPositions.size +
            node.positions.count { it !in expandedPositions } > MAX_EXPANDED_BLOCKS
        val resolved = node?.takeUnless { exceedsBudget }
        if (resolved != null) expandedPositions.addAll(resolved.positions)
        glowNodes[waypoint] = resolved
        return resolved
    }

    /**
     * Submits a whole matching live mining node, or the ordinary waypoint block when expansion is
     * unavailable or still scanning. Returns the same target as [waypointGlowTarget].
     * Higher [priority] wins when multiple visible waypoints request the same block.
     */
    fun drawWaypointGlowing(
        waypoint: EurybiumWaypoint,
        color: ChromaColour,
        priority: Int = 0,
    ): Vec3 {
        val level = getMinecraft().level ?: return waypoint.location.center
        val node = glowNode(waypoint)
        if (node == null) {
            submitGlow(GlowingBlock(waypoint.location), color, priority)
            return waypoint.location.center
        }

        for (block in node.blocks) {
            val pos = block.position
            if (!level.hasChunk(pos.x shr 4, pos.z shr 4)) continue
            val state = level.getBlockState(pos)
            if (DynamicMiningNodes.materialAt(BuiltInRegistries.BLOCK.getKey(state.block).toString()) != node.material) continue
            submitGlow(block, color, priority)
        }

        return node.center
    }

    private fun submitGlow(block: GlowingBlock, color: ChromaColour, priority: Int) {
        val previous = glowPriorities[block.position]
        if (previous != null && previous >= priority) return
        glowPriorities[block.position] = priority
        GlowingBlockRenderer.blocks.addExclusive(color, block)
    }

    companion object {
        private const val MAX_EXPANDED_BLOCKS = 2048
    }
}
