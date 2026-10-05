package github.businessdirt.eurybium.core.utils

import gg.essential.universal.UMinecraft.getMinecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import java.text.NumberFormat
import java.util.*

object MathUtils {

    fun Number.addSeparators(): String =
        NumberFormat.getNumberInstance(Locale.US).format(this)

    fun BlockPos.distanceToPlayer(): Double =
        getMinecraft().player?.position()?.distanceTo(Vec3(x.toDouble(), y.toDouble(), z.toDouble())) ?: 1.0
}
