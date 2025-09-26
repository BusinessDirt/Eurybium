package github.businessdirt.eurybium.utils

import gg.essential.universal.UMinecraft.getMinecraft
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.text.NumberFormat
import java.util.Locale

object MathUtils {

    fun Number.addSeparators(): String =
        NumberFormat.getNumberInstance(Locale.US).format(this)

    fun BlockPos.distanceSqToPlayer(): Double =
        getMinecraft().player?.pos?.squaredDistanceTo(x.toDouble(), y.toDouble(), z.toDouble()) ?: -1.0

    fun BlockPos.distanceToPlayer(): Double =
        getMinecraft().player?.pos?.distanceTo(Vec3d(x.toDouble(), y.toDouble(), z.toDouble())) ?: 1.0
}