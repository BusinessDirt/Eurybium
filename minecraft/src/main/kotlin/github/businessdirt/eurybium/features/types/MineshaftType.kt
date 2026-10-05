package github.businessdirt.eurybium.features.types

import github.businessdirt.eurybium.core.types.MinecraftColor

enum class MineshaftType(val color: MinecraftColor, val rawName: String) {
    UNKNOWN(MinecraftColor.WHITE, "Unknown"),
    TOPA1(MinecraftColor.YELLOW, "Topaz"),
    SAPP1(MinecraftColor.BLUE, "Sapphire"),
    AMET1(MinecraftColor.DARK_PURPLE, "Amethyst"),
    AMBE1(MinecraftColor.GOLD, "Amber"),
    JADE1(MinecraftColor.GREEN, "Jade"),
    TITA1(MinecraftColor.GRAY, "Titanium"),
    UMBE1(MinecraftColor.GOLD, "Umber"),
    TUNG1(MinecraftColor.DARK_GRAY, "Tungsten"),
    FAIR1(MinecraftColor.WHITE, "Vanguard"),
    RUBY1(MinecraftColor.RED, "Ruby"),
    RUBY2(MinecraftColor.RED, "Ruby Crystal"),
    ONYX1(MinecraftColor.BLACK, "Onyx"),
    ONYX2(MinecraftColor.BLACK, "Onyx Crystal"),
    AQUA1(MinecraftColor.DARK_BLUE, "Aquamarine"),
    AQUA2(MinecraftColor.DARK_BLUE, "Aquamarine Crystal"),
    CITR1(MinecraftColor.YELLOW, "Citrine"),
    CITR2(MinecraftColor.YELLOW, "Citrine Crystal"),
    PERI1(MinecraftColor.DARK_GREEN, "Peridot"),
    PERI2(MinecraftColor.DARK_GREEN, "Peridot Crystal"),
    JASP1(MinecraftColor.LIGHT_PURPLE, "Jasper"),
    JASP2(MinecraftColor.LIGHT_PURPLE, "Jasper Crystal"),
    OPAL1(MinecraftColor.WHITE, "Opal"),
    OPAL2(MinecraftColor.WHITE, "Opal Crystal")
    ;

    val displayName: String = color.getChatColor() + rawName

    val typeIndex: String = if (rawName.endsWith("Crystal")) "Crystal" else rawName

    val isGemstone: Boolean get() = when(this) {
            TITA1 -> false
            TUNG1 -> false
            UMBE1 -> false
            FAIR1 -> false
            else -> true
        }
}