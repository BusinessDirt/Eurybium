package github.businessdirt.eurybium.core.utils.text

import java.text.NumberFormat
import java.util.*

object NumberFormatting {

    fun Number.addSeparators(): String =
        NumberFormat.getNumberInstance(Locale.US).format(this)
}
