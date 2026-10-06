package github.businessdirt.eurybium.core.utils.text

import java.text.NumberFormat
import java.util.Locale

/** Consistent US-style number formatting regardless of the system locale. */
object NumberFormatting {

    // NumberFormat is mutable and not thread-safe; reuse one formatter per calling thread.
    private val formatter = ThreadLocal.withInitial { NumberFormat.getNumberInstance(Locale.US) }

    /** Formats this number with grouping separators and NumberFormat's default fractional precision. */
    fun Number.addSeparators(): String = formatter.get().format(this)
}
