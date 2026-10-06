package github.businessdirt.eurybium.api.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeveloperCommandsTest {
    @Test
    fun `developer commands and aliases follow live config for execution and suggestions`() {
        val previous = EurybiumMod.config.dev.devCommands
        try {
            EurybiumMod.config.dev.devCommands = false
            val dispatcher = CommandDispatcher<FabricClientCommandSource>()
            val source = Proxy.newProxyInstance(
                FabricClientCommandSource::class.java.classLoader, arrayOf(FabricClientCommandSource::class.java),
            ) { _, method, _ -> error("Unexpected source call: ${method.name}") } as FabricClientCommandSource
            var executions = 0
            val registration = CommandRegistrationEvent(dispatcher)
            for (category in CommandCategory.entries) {
                registration.register(category.name.lowercase()) {
                    this.category = category
                    description = "Test command"
                    aliases.add("alias_${category.name.lowercase()}")
                    simpleCallback { executions++ }
                    literal("child") { simpleCallback { executions++ } }
                }
            }
            // Reproduce Fabric's copy: requirements and executors are replaced,
            // while redirects are remapped and descendants are copied separately.
            val suggestionsDispatcher = CommandDispatcher<FabricClientCommandSource>()
            val copies = mutableMapOf<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>>()
            fun copyChildren(from: CommandNode<FabricClientCommandSource>, to: CommandNode<FabricClientCommandSource>) {
                for (child in from.children) {
                    if (!child.canUse(source)) continue
                    val builder = child.createBuilder().requires { true }
                    if (child.command != null) builder.executes { 0 }
                    child.redirect?.let { builder.redirect(copies.getValue(it)) }
                    val copy = builder.build()
                    copies[child] = copy
                    to.addChild(copy)
                    copyChildren(child, copy)
                }
            }
            copyChildren(dispatcher.root, suggestionsDispatcher.root)
            fun suggestions(input: String) = suggestionsDispatcher.getCompletionSuggestions(
                suggestionsDispatcher.parse(input, source),
            ).join().list.map { it.text }

            for (enabled in listOf(false, true, false, true)) {
                EurybiumMod.config.dev.devCommands = enabled
                for (category in CommandCategory.entries) {
                    val allowed = enabled || category !in CommandCategory.developmentCategories
                    for (name in listOf(category.name.lowercase(), "alias_${category.name.lowercase()}")) {
                        assertEquals(allowed, name in suggestions(""), name)
                        assertEquals(allowed, "child" in suggestions("$name "), name)
                        for (suffix in listOf("", " child")) {
                            val before = executions
                            if (allowed) dispatcher.execute(name + suffix, source)
                            else assertFailsWith<CommandSyntaxException> { dispatcher.execute(name + suffix, source) }
                            assertEquals(before + if (allowed) 1 else 0, executions)
                        }
                    }
                }
            }
            val stale = dispatcher.parse("alias_developer_test child", source)
            EurybiumMod.config.dev.devCommands = false
            val before = executions
            assertFailsWith<CommandSyntaxException> { dispatcher.execute(stale) }
            assertEquals(before, executions)
            assertTrue(suggestions("developer_").isEmpty())
        } finally {
            EurybiumMod.config.dev.devCommands = previous
        }
    }
}
