package github.businessdirt.eurybium.core.commands

import com.mojang.brigadier.CommandDispatcher
import github.businessdirt.eurybium.core.commands.brigadier.BrigadierRootBuilder
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.test.*

class CommandDataTest {
    @Test
    fun `only developer categories may omit descriptions`() {
        for (category in CommandCategory.entries) {
            val command = BrigadierRootBuilder<FabricClientCommandSource>("test").apply { this.category = category }
            if (category in CommandCategory.developmentCategories) command.checkDescriptionAndCategory()
            else assertFailsWith<RuntimeException> { command.checkDescriptionAndCategory() }
            command.description = "Test"
            command.checkDescriptionAndCategory()
        }
    }

    @Test
    fun `metadata sorts by category then name and includes aliases without mutating them`() {
        val event = CommandRegistrationEvent(CommandDispatcher())
        for ((name, category) in listOf("z" to CommandCategory.USERS_ACTIVE, "b" to CommandCategory.MAIN, "a" to CommandCategory.MAIN)) {
            event.register(name) { description = "Test"; this.category = category; aliases.add("alias_$name") }
        }
        assertEquals(listOf("a", "b", "z"), event.commands.map { it.name })
        assertEquals(listOf("a", "alias_a"), event.commands.first().allNames())
        assertEquals(listOf("alias_a"), event.commands.first().aliases)
    }

    @Test
    fun `registration rejects every canonical name and alias collision before changing the tree`() {
        for ((name, aliases) in listOf("first" to emptyList(), "other" to listOf("first"), "alias" to emptyList(), "other" to listOf("alias"), "other" to listOf("other"), "other" to listOf("dup", "dup"))) {
            val dispatcher = CommandDispatcher<FabricClientCommandSource>()
            val event = CommandRegistrationEvent(dispatcher)
            event.register("first") { description = "Test"; this.aliases.add("alias") }
            assertFailsWith<RuntimeException>("$name $aliases") {
                event.register(name) { description = "Test"; this.aliases.addAll(aliases) }
            }
            assertEquals(listOf("first"), event.commands.map { it.name })
            assertEquals(setOf("first", "alias"), dispatcher.root.children.map { it.name }.toSet())
        }
    }

    @Test
    fun `missing description fails registration without adding nodes`() {
        val dispatcher = CommandDispatcher<FabricClientCommandSource>()
        val event = CommandRegistrationEvent(dispatcher)
        assertFailsWith<RuntimeException> { event.register("missing") {} }
        assertTrue(event.commands.isEmpty())
        assertTrue(dispatcher.root.children.isEmpty())
    }
}
