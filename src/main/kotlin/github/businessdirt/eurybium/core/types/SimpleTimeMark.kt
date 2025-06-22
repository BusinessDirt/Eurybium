package github.businessdirt.eurybium.core.types

import github.businessdirt.eurybium.EurybiumMod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@JvmInline
value class SimpleTimeMark(private val millis: Long) : Comparable<SimpleTimeMark> {
    operator fun minus(other: SimpleTimeMark): Duration =
        (millis - other.millis).milliseconds

    operator fun plus(other: Duration): SimpleTimeMark =
        SimpleTimeMark(millis + other.inWholeMilliseconds)

    operator fun minus(other: Duration): SimpleTimeMark =
        plus(-other)

    fun passedSince(): Duration = now() - this
    fun timeUntil(): Duration = -passedSince()

    fun isInPast(): Boolean = timeUntil().isNegative()
    fun isInFuture(): Boolean = timeUntil().isPositive()
    fun isFarPast(): Boolean = millis == 0L
    fun isFarFuture(): Boolean = millis == Long.MAX_VALUE

    fun takeIfInitialized(): SimpleTimeMark? =
        if (isFarPast() || isFarFuture()) null else this

    fun absoluteDifference(other: SimpleTimeMark): Duration =
        abs(millis - other.millis).milliseconds

    override fun compareTo(other: SimpleTimeMark): Int =
        millis.compareTo(other.millis)

    override fun toString(): String = when (this) {
        farPast() -> "The Far Past"
        farFuture() -> "The Far Future"
        else -> Instant.ofEpochMilli(millis).toString()
    }

    private fun String.applyTimeFormat(): String =
        if (EurybiumMod.config.gui.timeFormat24h) {
            replace("h", "H").replace("a", "")
        } else this

    fun formattedDate(pattern: String): String {
        val newPattern = pattern.applyTimeFormat()
        val instant = Instant.ofEpochMilli(millis)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern(newPattern.trim())
        return localDateTime.format(formatter)
    }

    fun toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

    fun toMillis() = millis

    fun toLocalDate(): LocalDate = toLocalDateTime().toLocalDate()

    companion object {

        fun now(): SimpleTimeMark = SimpleTimeMark(System.currentTimeMillis())

        fun farPast(): SimpleTimeMark = SimpleTimeMark(0)
        fun farFuture(): SimpleTimeMark = SimpleTimeMark(Long.MAX_VALUE)

        fun Duration.fromNow(): SimpleTimeMark = now() + this

        fun Long.asTimeMark(): SimpleTimeMark = SimpleTimeMark(this)
    }
}