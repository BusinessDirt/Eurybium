package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.annotations.Expose
import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.core.rendering.GlowingBlockRenderer
import github.businessdirt.eurybium.config.GemstoneNode
import github.businessdirt.eurybium.config.features.mining.OrderedWaypointsConfig
import github.businessdirt.eurybium.features.types.MineshaftType
import github.businessdirt.eurybium.core.utils.MathUtils.distanceToPlayer
import net.minecraft.core.BlockPos

class EurybiumWaypoint(
    @Expose val location: BlockPos,
    @Expose var number: Int,
    @Expose val options: MutableMap<String, String> = mutableMapOf(),
) {
    @Transient
    var nearestNodeIndex: Int? = null

    fun copy() = EurybiumWaypoint(location, number, options)

    fun distanceToPlayer(renderMode: OrderedWaypointsConfig.RenderMode, mineshaftType: MineshaftType): Double {
        val player = getMinecraft().player ?: return 0.0
        if (renderMode == OrderedWaypointsConfig.RenderMode.GLOW && nearestNodeIndex != null) {
            return GlowingBlockRenderer.gemstoneNodes.mineshaftNodes!![mineshaftType.typeIndex]?.get(nearestNodeIndex!!)?.centerVec()
                ?.distanceTo(player.position()) ?: 0.0
        }

        return location.distanceToPlayer()
    }

    fun getNearestNode(mineshaftType: MineshaftType): GemstoneNode? {
        val nodes = GlowingBlockRenderer.gemstoneNodes.mineshaftNodes?.get(mineshaftType.typeIndex) ?: return null

        nearestNodeIndex?.let { idx ->
            if (idx in nodes.indices) {
                return nodes[idx]
            }
        }

        var bestIndex = -1
        var bestDist = Double.MAX_VALUE

        for ((i, node) in nodes.withIndex()) {
            if (node.centerNodeIndex !in node.blocks.indices) continue

            val centerBlock = node.blocks.elementAt(node.centerNodeIndex)
            val dist = centerBlock.position.distToCenterSqr(location.getCenter())

            if (dist < bestDist) {
                bestDist = dist
                bestIndex = i
            }
        }

        if (bestIndex >= 0) {
            nearestNodeIndex = bestIndex
            return nodes[bestIndex]
        }

        return null
    }
}