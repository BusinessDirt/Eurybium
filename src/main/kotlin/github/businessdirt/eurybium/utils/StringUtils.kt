package github.businessdirt.eurybium.utils

import github.businessdirt.eurybium.utils.MathUtils.addSeparators
import kotlin.math.min

object StringUtils {
    private val COLOR_CHARS = mutableSetOf<Char?>('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    private val FORMATTING_CHARS = mutableSetOf<Char?>('k', 'l', 'm', 'n', 'o', 'r')

    fun String.optionalAn(): String {
        @Suppress("SpellCheckingInspection")
        return if ("aeiou".contains(this.trim { it <= ' ' }[0].toString().lowercase())) "an" else "a"
    }

    fun String.hasWhitespace(): Boolean {
        return this.chars().anyMatch { codePoint: Int -> Character.isWhitespace(codePoint) }
    }

    fun String.splitFirst(char: Char): Pair<String, String> {
        val firstIndex = this.indexOf(char)
        return if (firstIndex == -1) this to ""
        else this.substring(0, firstIndex) to this.substring(firstIndex + 1)
    }

    fun String.splitFirstWhitespace(): Pair<String, String> = splitFirst(' ')

    fun String.splitLast(char: Char): Pair<String, String> {
        val lastIndex = this.lastIndexOf(char)
        return if (lastIndex == -1) "" to this
        else this.substring(0, lastIndex) to this.substring(lastIndex + 1)
    }

    fun String.splitLastWhitespace(): Pair<String, String> = splitLast(' ')

    /**
     * Removes color and optionally formatting codes from the given string, leaving plain text.
     *
     * @param keepFormatting  If true, keeps non-color formatting codes (bold, italic, etc.)
     * @return A string with color codes removed (and optionally formatting codes)
     */
    fun CharSequence.removeColor(keepFormatting: Boolean = false): String {
        var nextFormattingSequence = this.toString().indexOf('§')
        if (nextFormattingSequence < 0) return this.toString()

        val cleanedString = StringBuilder(this.length)
        var readIndex = 0
        var isFormatted = false

        while (nextFormattingSequence >= 0) {
            // Append everything before the next § symbol
            cleanedString.append(this, readIndex, nextFormattingSequence)

            // Get the formatting code character after §
            val formattingCode = if (nextFormattingSequence + 1 < this.length) this[nextFormattingSequence + 1]
                .lowercaseChar() else 0.toChar()

            if (keepFormatting && FORMATTING_CHARS.contains(formattingCode)) {
                isFormatted = formattingCode != 'r'
                // Keep the § code in the string
                readIndex = nextFormattingSequence
                nextFormattingSequence = this.toString().indexOf('§', readIndex + 1)
            } else {
                // Skip this formatting sequence
                if (isFormatted && COLOR_CHARS.contains(formattingCode)) {
                    cleanedString.append("§r")
                    isFormatted = false
                }

                readIndex = nextFormattingSequence + 2
                nextFormattingSequence = this.toString().indexOf('§', readIndex)
                readIndex = min(readIndex, this.length)
            }
        }

        // Append remaining text after the last formatting sequence
        cleanedString.append(this, readIndex, this.length)

        return cleanedString.toString()
    }

    fun String.stripLeadingAndTrailingColorResetFormatting(): String {
        var message = this
        while (message.startsWith("§r")) message = message.substring(2)
        while (message.endsWith("§r")) message = message.dropLast(2)
        return message
    }

    fun pluralize(number: Int, singular: String, plural: String? = null, withNumber: Boolean = false): String {
        val pluralForm = plural ?: "${singular}s"
        var str = if (number == 1 || number == -1) singular else pluralForm
        if (withNumber) str = "${number.addSeparators()} $str"
        return str
    }
}
