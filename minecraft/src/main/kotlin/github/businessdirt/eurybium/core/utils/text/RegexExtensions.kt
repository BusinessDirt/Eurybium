package github.businessdirt.eurybium.core.utils.text

import java.util.regex.Matcher
import java.util.regex.Pattern

object RegexExtensions {

    inline fun <T> Pattern.matchMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.matches()) consumer(it) else null }
}
