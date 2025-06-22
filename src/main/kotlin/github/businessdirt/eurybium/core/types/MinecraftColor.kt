package github.businessdirt.eurybium.core.types

import java.awt.Color

enum class MinecraftColor(val chatColorCode: Char, private val color: Color, private val coloredLabel: String) {
    BLACK('0', Color(0, 0, 0), "§0Black"),
    DARK_BLUE('1', Color(0, 0, 170), "§1Dark Blue"),
    DARK_GREEN('2', Color(0, 170, 0), "§2Dark Green"),
    DARK_AQUA('3', Color(0, 170, 170), "§3Dark Aqua"),
    DARK_RED('4', Color(170, 0, 0), "§4Dark Red"),
    DARK_PURPLE('5', Color(170, 0, 170), "§5Dark Purple"),
    GOLD('6', Color(255, 170, 0), "§6Gold"),
    GRAY('7', Color(170, 170, 170), "§7Gray"),
    DARK_GRAY('8', Color(85, 85, 85), "§8Dark Gray"),
    BLUE('9', Color(85, 85, 255), "§9Blue"),
    GREEN('a', Color(85, 255, 85), "§aGreen"),
    AQUA('b', Color(85, 255, 255), "§bAqua"),
    RED('c', Color(255, 85, 85), "§cRed"),
    LIGHT_PURPLE('d', Color(255, 85, 255), "§dLight Purple"),
    YELLOW('e', Color(255, 255, 85), "§eYellow"),
    WHITE('f', Color(255, 255, 255), "§fWhite")
    ;

    fun getChatColor(): String = "§$chatColorCode"
}