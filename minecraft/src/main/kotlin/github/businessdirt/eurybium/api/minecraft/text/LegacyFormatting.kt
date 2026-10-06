package github.businessdirt.eurybium.api.minecraft.text

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.*

object LegacyFormatting {

    fun Component.legacyString(): String = buildString {
        visit({ style, text ->
            append("§r")
            style.color?.let { color ->
                ChatFormatting.entries.firstOrNull { it.isColor && it.color == color.value }?.let { append(it) }
            }
            if (style.isBold) append("§l")
            if (style.isItalic) append("§o")
            if (style.isUnderlined) append("§n")
            if (style.isStrikethrough) append("§m")
            if (style.isObfuscated) append("§k")
            append(text)
            Optional.empty<Unit>()
        }, Style.EMPTY)
    }
}
