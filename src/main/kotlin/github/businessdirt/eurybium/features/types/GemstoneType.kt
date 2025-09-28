package github.businessdirt.eurybium.features.types

import github.businessdirt.eurybium.core.types.MinecraftColor
import kotlin.math.round

@Suppress("unused")
enum class GemstoneType(val breakingPower: Int, val blockStrength: Int, val color: MinecraftColor) {
    UNKNOWN(-1, -1, MinecraftColor.RED),
    RUBY(6, 2300, MinecraftColor.RED),
    AMBER(7, 3000, MinecraftColor.GOLD),
    SAPPHIRE(7, 3000, MinecraftColor.BLUE),
    JADE(7, 3000, MinecraftColor.GREEN),
    AMETHYST(7, 3000, MinecraftColor.DARK_PURPLE),
    OPAL(7, 3000, MinecraftColor.WHITE),
    TOPAZ(8, 3800, MinecraftColor.YELLOW),
    JASPER(9, 4800, MinecraftColor.LIGHT_PURPLE),
    ONYX(9, 5200, MinecraftColor.BLACK),
    AQUAMARINE(9, 5200, MinecraftColor.BLACK),
    CITRINE(9, 5200, MinecraftColor.YELLOW),
    PERIDOT(9, 5200, MinecraftColor.DARK_GREEN)
    ;

    val displayName get() = color.getChatColor() + this.name.lowercase().replaceFirstChar(Char::uppercase)
    fun isValid(): Boolean = this != UNKNOWN

    fun ticksToBreak(miningSpeed: Int): Int = round((30F * blockStrength) / miningSpeed).toInt()

    companion object {
        fun fromMineshaftType(type: MineshaftType): GemstoneType? =
            entries.find { it.name == type.rawName.replace(" Crystal", "").uppercase() }
    }
}