package github.businessdirt.eurybium.api.minecraft.world

import gg.essential.universal.UMinecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class BlockPosExtensions {

    fun BlockPos.distanceToPlayer(): Double =
        UMinecraft.getMinecraft().player?.position()?.distanceTo(Vec3(x.toDouble(), y.toDouble(), z.toDouble())) ?: 1.0
}
