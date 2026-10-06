package github.businessdirt.eurybium.core.utils.text

import java.util.regex.Matcher
import java.util.regex.Pattern

/** Helpers for reading groups from complete Java regex matches. */
object RegexExtensions {

    /** Invokes [consumer] only if the entire [text] matches, or returns null on a mismatch. */
    inline fun <T> Pattern.matchMatcher(text: String, consumer: Matcher.() -> T): T? {
        val matcher = matcher(text)
        return if (matcher.matches()) matcher.consumer() else null
    }
}
