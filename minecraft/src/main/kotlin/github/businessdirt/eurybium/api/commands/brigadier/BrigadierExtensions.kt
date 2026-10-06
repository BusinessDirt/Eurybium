package github.businessdirt.eurybium.api.commands.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider

/** Extensions shared by the command builders. */
object BrigadierExtensions {

    /** Whether this argument consumes the entire remaining command text. */
    fun ArgumentType<*>.isGreedy(): Boolean =
        this is StringArgumentType && type == StringArgumentType.StringType.GREEDY_PHRASE

    /** Suggests current collection entries matching the input prefix, ignoring case while preserving spelling. */
    fun <S> Collection<String>.toSuggestionProvider(): SuggestionProvider<S> = SuggestionProvider { _, builder ->
        for (option in this) {
            if (option.startsWith(builder.remaining, ignoreCase = true)) {
                builder.suggest(option)
            }
        }

        builder.buildFuture()
    }
}
