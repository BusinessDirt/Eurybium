package github.businessdirt.eurybium.features.waypoints

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType

/** Accepts unquoted namespace IDs and quoted user names; Brigadier's plain string stops at a colon. */
internal object WaypointRouteArgument : ArgumentType<String> {
    override fun parse(reader: StringReader): String {
        if (reader.canRead() && StringReader.isQuotedStringStart(reader.peek())) return reader.readString()
        val start = reader.cursor
        while (reader.canRead() && !reader.peek().isWhitespace()) reader.skip()
        return reader.string.substring(start, reader.cursor)
    }

    override fun getExamples(): Collection<String> = listOf("eurybium:JASP1", "my-route", "\"my route\"")
}
