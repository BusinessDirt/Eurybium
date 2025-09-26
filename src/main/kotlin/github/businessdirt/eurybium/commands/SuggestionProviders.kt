package github.businessdirt.eurybium.commands

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object SuggestionProviders {

    fun dynamic(supplier: () -> Collection<String>): SuggestionProvider<FabricClientCommandSource> {
        return SuggestionProvider { _, builder ->
            val remaining = builder.remainingLowerCase
            for (option in supplier()) {
                if (option.startsWith(remaining)) {
                    builder.suggest(option)
                }
            }
            builder.buildFuture()
        }
    }
}