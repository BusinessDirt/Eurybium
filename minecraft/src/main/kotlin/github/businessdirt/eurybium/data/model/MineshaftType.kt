package github.businessdirt.eurybium.data.model

import github.businessdirt.eurybium.core.types.MinecraftColor

enum class MineshaftType(val color: MinecraftColor, val rawName: String) {
    TOPA_1(MinecraftColor.YELLOW, "Topaz 1"),
    TOPA_2(MinecraftColor.YELLOW, "Topaz 2"),
    SAPP_1(MinecraftColor.BLUE, "Sapphire 1"),
    SAPP_2(MinecraftColor.BLUE, "Sapphire 2"),
    AMET_1(MinecraftColor.DARK_PURPLE, "Amethyst 1"),
    AMET_2(MinecraftColor.DARK_PURPLE, "Amethyst 2"),
    AMBE_1(MinecraftColor.GOLD, "Amber 1"),
    AMBE_2(MinecraftColor.GOLD, "Amber 2"),
    JADE_1(MinecraftColor.GREEN, "Jade 1"),
    JADE_2(MinecraftColor.GREEN, "Jade 2"),
    TITA_1(MinecraftColor.GRAY, "Titanium"),
    UMBE_1(MinecraftColor.GOLD, "Umber"),
    TUNG_1(MinecraftColor.DARK_GRAY, "Tungsten"),
    FAIR_1(MinecraftColor.WHITE, "Vanguard"),
    RUBY_1(MinecraftColor.RED, "Ruby 1"),
    RUBY_2(MinecraftColor.RED, "Ruby 2"),
    RUBY_C(MinecraftColor.RED, "Ruby Crystal"),
    ONYX_1(MinecraftColor.BLACK, "Onyx 1"),
    ONYX_2(MinecraftColor.BLACK, "Onyx 2"),
    ONYX_C(MinecraftColor.BLACK, "Onyx Crystal"),
    AQUA_1(MinecraftColor.DARK_BLUE, "Aquamarine 1"),
    AQUA_2(MinecraftColor.DARK_BLUE, "Aquamarine 2"),
    AQUA_C(MinecraftColor.DARK_BLUE, "Aquamarine Crystal"),
    CITR_1(MinecraftColor.YELLOW, "Citrine 1"),
    CITR_2(MinecraftColor.YELLOW, "Citrine 2"),
    CITR_C(MinecraftColor.YELLOW, "Citrine Crystal"),
    PERI_1(MinecraftColor.DARK_GREEN, "Peridot 1"),
    PERI_2(MinecraftColor.DARK_GREEN, "Peridot 2"),
    PERI_C(MinecraftColor.DARK_GREEN, "Peridot Crystal"),
    JASP_1(MinecraftColor.LIGHT_PURPLE, "Jasper"),
    JASP_C(MinecraftColor.LIGHT_PURPLE, "Jasper Crystal"),
    OPAL_1(MinecraftColor.WHITE, "Opal"),
    OPAL_C(MinecraftColor.WHITE, "Opal Crystal"),
    LITT_L(MinecraftColor.AQUA, "Littlefoot's Den"),
    ;

    val displayName: String = color.getChatColor() + rawName
    override fun toString() = displayName
}
