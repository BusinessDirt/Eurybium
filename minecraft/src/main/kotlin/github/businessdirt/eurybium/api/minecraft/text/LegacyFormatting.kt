package github.businessdirt.eurybium.api.minecraft.text

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.Optional

/** Conversion and cleanup of section-sign legacy Minecraft formatting. */
object LegacyFormatting {

    private const val COLORS = "0123456789abcdef"
    private const val FORMATTING = "klmnor"
    private val legacyColors = ChatFormatting.entries.filter { it.isColor }.associateBy { it.color }

    /**
     * Flattens text and inherited styles into legacy codes, resetting before each component segment.
     * Named legacy colors are preserved; arbitrary RGB colors cannot be represented by this format.
     */
    fun Component.legacyString(): String = buildString {
        visit({ style, text ->
            append("§r")
            style.color?.let { color -> legacyColors[color.value]?.let { append(it) } }
            if (style.isBold) append("§l")
            if (style.isItalic) append("§o")
            if (style.isUnderlined) append("§n")
            if (style.isStrikethrough) append("§m")
            if (style.isObfuscated) append("§k")
            append(text)
            Optional.empty<Unit>()
        }, Style.EMPTY)
    }

    /**
     * Removes section-sign codes, including malformed pairs and a trailing section sign.
     * If [keepFormatting] is true, keeps bold/italic/etc. codes and resets them when removing a color.
     */
    fun CharSequence.removeColor(keepFormatting: Boolean = false): String {
        // Snapshot mutable CharSequences once; repeated toString calls inside the scan copy the whole input.
        val text = toString()
        if ('§' !in text) return text

        return buildString(text.length) {
            var index = 0
            var formatted = false

            while (index < text.length) {
                if (text[index] != '§') {
                    append(text[index++])
                    continue
                }

                val originalCode = text.getOrNull(index + 1)
                val code = originalCode?.lowercaseChar()

                if (keepFormatting && code != null && code in FORMATTING) {
                    append('§').append(originalCode)
                    formatted = code != 'r'
                } else if (formatted && code != null && code in COLORS) {
                    // A color code resets styles in Minecraft, even when the color itself is stripped.
                    append("§r")
                    formatted = false
                }

                index += 2
            }
        }
    }

    /** Removes repeated lowercase reset codes from the edges while preserving resets inside the text. */
    fun String.stripLeadingAndTrailingColorResetFormatting(): String {
        var start = 0
        var end = length

        while (start + 1 < end && startsWith("§r", start)) start += 2
        while (end - 2 >= start && startsWith("§r", end - 2)) end -= 2

        return substring(start, end)
    }
}
