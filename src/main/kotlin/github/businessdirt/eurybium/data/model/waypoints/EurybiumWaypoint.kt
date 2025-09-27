package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.rendering.GlowingBlock
import github.businessdirt.eurybium.features.types.MineshaftType
import net.minecraft.util.math.BlockPos

class EurybiumWaypoint(
    @Expose val location: BlockPos,
    @Expose var number: Int,
    @Expose val options: MutableMap<String, String> = mutableMapOf(),
) : Copyable<EurybiumWaypoint> {
    @Transient
    var nearestNodeIndex: Int? = null

    override fun copy() = EurybiumWaypoint(location, number, options)

    fun getNearestNode(mineshaftType: MineshaftType): Collection<GlowingBlock>? {
        val nodes = EurybiumMod.gemstoneNodes.mineshaftNodes?.get(mineshaftType.typeIndex) ?: return null

        nearestNodeIndex?.let { idx ->
            if (idx in nodes.indices) {
                return nodes[idx]
            }
        }

        var bestIndex = -1
        var bestDist = Double.MAX_VALUE

        for ((i, node) in nodes.withIndex()) {
            val dist = node.minOfOrNull { it.position.getSquaredDistance(location.toCenterPos()) }
                ?: Double.MAX_VALUE
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