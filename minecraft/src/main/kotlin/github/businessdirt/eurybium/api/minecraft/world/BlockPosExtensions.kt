package github.businessdirt.eurybium.api.minecraft.world

import gg.essential.universal.UMinecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

/** Distance helpers for world block positions. */
object BlockPosExtensions {

    /**
     * Distance from this block's integer position to the local player, or positive infinity if absent.
     * Infinity prevents range checks from treating an unavailable player as nearby.
     */
    fun BlockPos.distanceToPlayer(): Double {
        val player = UMinecraft.getMinecraft().player ?: return Double.POSITIVE_INFINITY
        return player.position().distanceTo(Vec3(x.toDouble(), y.toDouble(), z.toDouble()))
    }
}
