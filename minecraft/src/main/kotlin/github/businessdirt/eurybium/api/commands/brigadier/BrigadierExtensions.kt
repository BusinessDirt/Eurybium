package github.businessdirt.eurybium.api.commands.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider

object BrigadierExtensions {

    fun <T> ArgumentType<T>.isGreedy(): Boolean {
        return when (this) {
            is StringArgumentType -> this.type == StringArgumentType.StringType.GREEDY_PHRASE
            else -> false
        }
    }

    fun <S> Collection<String>.toSuggestionProvider() = SuggestionProvider<S> { _, builder ->
        for (s in this) {
            if (s.startsWith(builder.remainingLowerCase)) {
                builder.suggest(s)
            }
        }
        builder.buildFuture()
    }
}
