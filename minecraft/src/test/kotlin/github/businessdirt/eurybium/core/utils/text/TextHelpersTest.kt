package github.businessdirt.eurybium.core.utils.text

import github.businessdirt.eurybium.core.utils.text.StringExtensions.optionalAn
import github.businessdirt.eurybium.core.utils.text.StringExtensions.hasWhitespace
import github.businessdirt.eurybium.core.utils.text.StringExtensions.splitFirst
import github.businessdirt.eurybium.core.utils.text.StringExtensions.splitLast
import github.businessdirt.eurybium.core.utils.text.StringExtensions.splitFirstWhitespace
import github.businessdirt.eurybium.core.utils.text.StringExtensions.splitLastWhitespace
import github.businessdirt.eurybium.core.utils.text.NumberFormatting.addSeparators
import github.businessdirt.eurybium.core.utils.text.RegexExtensions.matchMatcher
import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import kotlin.test.*

class TextHelpersTest {

    @Test
    fun `blank articles and Unicode whitespace splits are consistent`() {
        assertEquals("a", "".optionalAn())
        assertEquals("a", "   ".optionalAn())
        assertEquals("an", "\tApple".optionalAn())
        assertEquals("first" to "second", "first\tsecond".splitFirstWhitespace())
        assertEquals("first" to "second", "first\nsecond".splitLastWhitespace())
        assertTrue("first\u00a0second".hasWhitespace())
        assertEquals("first" to "second", "first\u00a0second".splitFirstWhitespace())
        assertEquals("text" to "", "text".splitFirst(':'))
        assertEquals("" to "text", "text".splitLast(':'))
    }

    @Test
    fun `pluralization and number grouping preserve existing display behavior`() {
        assertEquals("-1 apple", StringExtensions.pluralize(-1, "apple", withNumber = true))
        assertEquals("2 mice", StringExtensions.pluralize(2, "mouse", "mice", withNumber = true))
        assertEquals("1,234,567", 1234567.addSeparators())
    }

    @Test
    fun `regex callbacks run only for an entire match`() {
        val pattern = Pattern.compile("(?<word>[a-z]+)")
        assertEquals("word", pattern.matchMatcher("word") { group("word") })
        var called = false
        assertNull(pattern.matchMatcher("word1") { called = true; group("word") })
        assertFalse(called)
    }
}
