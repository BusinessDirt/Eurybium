package github.businessdirt.eurybium.config

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.core.rendering.GlowingBlock
import net.minecraft.util.math.Vec3d

data class GemstoneNode(
    @Expose var centerNodeIndex: Int = -1,
    @Expose val blocks: MutableSet<GlowingBlock> = mutableSetOf(),
) {
    fun centerVec(): Vec3d = Vec3d(
        blocks.sumOf { it.position.x.toLong() }.toDouble() / blocks.size,
        blocks.sumOf { it.position.y.toLong() }.toDouble() / blocks.size,
        blocks.sumOf { it.position.z.toLong() }.toDouble() / blocks.size
    )

    fun getCenterPos(): Vec3d {
        if (centerNodeIndex == -1) return Vec3d.ZERO
        return blocks.elementAt(centerNodeIndex).position.toCenterPos()
    }
}

class GemstoneNodeData {

    @Expose
    var mineshaftNodes: MutableMap<String, MutableList<GemstoneNode>>? = null
}