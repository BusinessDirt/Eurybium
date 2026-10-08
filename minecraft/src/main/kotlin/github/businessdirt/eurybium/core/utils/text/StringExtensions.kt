package github.businessdirt.eurybium.core.utils.text

import github.businessdirt.eurybium.core.utils.text.NumberFormatting.addSeparators

/** General string splitting and simple English display helpers. */
object StringExtensions {

    /** Selects "an" for a leading vowel and "a" otherwise, including blank text; this is a spelling heuristic. */
    fun String.optionalAn(): String =
        if ((firstOrNull { !it.isWhitespace() }?.lowercaseChar() ?: ' ') in "aeiou") "an" else "a"

    /** Whether any character is whitespace, including tabs, line breaks, and Unicode space characters. */
    fun String.hasWhitespace(): Boolean = any(Char::isWhitespace)

    /** Splits at the first [char]; absent separators produce this string followed by an empty string. */
    fun String.splitFirst(char: Char): Pair<String, String> = splitAt(indexOf(char), first = true)

    /** Splits at the first whitespace character, removing exactly that one character. */
    fun String.splitFirstWhitespace(): Pair<String, String> = splitAt(indexOfFirst(Char::isWhitespace), first = true)

    /** Splits at the last [char]; absent separators produce an empty string followed by this string. */
    fun String.splitLast(char: Char): Pair<String, String> = splitAt(lastIndexOf(char), first = false)

    /** Splits at the last whitespace character, removing exactly that one character. */
    fun String.splitLastWhitespace(): Pair<String, String> = splitAt(indexOfLast(Char::isWhitespace), first = false)

    private fun String.splitAt(index: Int, first: Boolean): Pair<String, String> = when {
        index >= 0 -> substring(0, index) to substring(index + 1)
        first -> this to ""
        else -> "" to this
    }

    /** Uses the singular for 1 and -1, optionally prepending the formatted count. */
    fun pluralize(number: Int, singular: String, plural: String? = null, withNumber: Boolean = false): String {
        val word = if (number == 1 || number == -1) singular else plural ?: "${singular}s"
        return if (withNumber) "${number.addSeparators()} $word" else word
    }

    /**
     * Executes the [action] only if the string is not null and not empty.
     */
    fun String?.ifNotNullOrEmpty(action: (String) -> Unit) {
        if (!this.isNullOrEmpty()) action(this)
    }
}
