package github.businessdirt.eurybium.api.commands.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierExtensions.isGreedy
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierExtensions.toSuggestionProvider
import org.junit.jupiter.api.Test
import kotlin.test.*

class BrigadierExtensionsTest {

    @Test
    fun `suggestions ignore prefix case while keeping their original spelling`() {
        val dispatcher = CommandDispatcher<Any>()
        dispatcher.register(LiteralArgumentBuilder.literal<Any>("test").then(
            RequiredArgumentBuilder.argument<Any, String>("value", StringArgumentType.word())
                .suggests(listOf("Apple", "APRICOT", "banana").toSuggestionProvider())
        ))
        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("test ap", Any())).join()
        assertEquals(setOf("Apple", "APRICOT"), suggestions.list.map { it.text }.toSet())
        assertTrue(StringArgumentType.greedyString().isGreedy())
        assertFalse(StringArgumentType.word().isGreedy())
    }
}
