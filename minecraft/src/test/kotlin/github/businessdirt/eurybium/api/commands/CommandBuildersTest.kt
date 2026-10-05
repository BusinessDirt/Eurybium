package github.businessdirt.eurybium.api.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierArgument
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierArguments
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierRootBuilder
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.test.*

class CommandBuildersTest {
    private val dispatcher = CommandDispatcher<FabricClientCommandSource>()
    private val registration = CommandRegistrationEvent(dispatcher)
    private val source = commandSource()

    @Test
    fun `root alias literal chains and every argument accessor execute the same callback`() {
        val received = mutableListOf<Int>()
        registration.register("test") {
            description = "Test"
            aliases.add("alias")
            simpleCallback { received.add(0) }
            literalCallback("ping", "pong") { received.add(1) }
            literal("nested chain") {
                arg("number", BrigadierArguments.int()) { number ->
                    callback {
                        assertEquals(7, getArg(number))
                        assertEquals(7, get(number))
                        assertEquals(7, invoke(number))
                        assertEquals(7, getArgByName<Int>("number"))
                        assertEquals(7, number.get(this))
                        assertEquals(7, number(this))
                        received.add(number(this))
                    }
                }
            }
            argCallback("text value", BrigadierArguments.greedyString()) { received.add(it.length) }
        }
        for (name in listOf("test", "alias")) {
            for (suffix in listOf("", " ping", " pong", " nested chain 7", " text hello world")) {
                assertEquals(1, dispatcher.execute(name + suffix, source))
            }
        }
        assertEquals(listOf(0, 1, 1, 7, 11, 0, 1, 1, 7, 11), received)
        assertNotNull((registration.commands.single() as BrigadierRootBuilder<*>).node)
    }

    @Test
    fun `internal argument builder supports a literal path and a typed callback`() {
        var received: String? = null
        registration.register("internal") {
            description = "Test"
            internalArg("nested value", BrigadierArguments.word()) {
                callback { received = getArgByName<String>("value") }
            }
        }
        dispatcher.execute("internal nested hello", source)
        assertEquals("hello", received)
    }

    @Test
    fun `bounded arguments reject invalid input without invoking callbacks`() {
        var calls = 0
        registration.register("bounded") {
            description = "Test"
            argCallback("value", BrigadierArguments.int(-2, 2)) { calls++ }
        }
        for (input in listOf("-2", "0", "2")) dispatcher.execute("bounded $input", source)
        for (input in listOf("-3", "3", "text", "1 extra", "")) {
            assertFailsWith<CommandSyntaxException> { dispatcher.execute("bounded $input", source) }
        }
        assertEquals(3, calls)
    }

    @Test
    fun `greedy arguments forbid subsequent literals and arguments`() {
        for (appendLiteral in listOf(true, false)) {
            val root = BrigadierRootBuilder<FabricClientCommandSource>("test")
            assertFailsWith<IllegalStateException> {
                root.arg("text", BrigadierArguments.greedyString()) {
                    if (appendLiteral) literal("invalid") { simpleCallback {} }
                    else argCallback("invalid", BrigadierArguments.int()) {}
                }
            }
        }
    }

    @Test
    fun `collection and dynamic suggestions filter prefixes and refresh suppliers`() {
        var choices = listOf("alpha", "beta")
        registration.register("suggest") {
            description = "Test"
            argCallback("fixed", BrigadierArguments.word(), listOf("red", "rose", "blue")) {}
            literal("live") {
                argCallback("value", BrigadierArguments.word(), SuggestionProviders.dynamic { choices }) {}
            }
        }
        fun suggest(input: String) = dispatcher.getCompletionSuggestions(dispatcher.parse(input, source)).join().list.map { it.text }
        assertEquals(listOf("red", "rose"), suggest("suggest R"))
        assertEquals(listOf("alpha"), suggest("suggest live A"))
        choices = listOf("apple", "apricot")
        assertEquals(choices, suggest("suggest live a"))
        assertTrue(suggest("suggest live z").isEmpty())
    }

    @Test
    fun `argument factories support signed values booleans and all string modes`() {
        assertEquals(-3, BrigadierArguments.int().parse(StringReader("-3")))
        assertEquals(-4L, BrigadierArguments.long().parse(StringReader("-4")))
        assertEquals(-1.5, BrigadierArguments.double().parse(StringReader("-1.5")))
        assertEquals(-2.5f, BrigadierArguments.float().parse(StringReader("-2.5")))
        assertEquals(true, BrigadierArguments.bool().parse(StringReader("true")))
        assertEquals("two words", BrigadierArguments.string().parse(StringReader("\"two words\"")))
        assertEquals("two", BrigadierArguments.word().parse(StringReader("two words")))
        assertEquals("two words", BrigadierArguments.greedyString().parse(StringReader("two words")))
        assertEquals(StringArgumentType.StringType.GREEDY_PHRASE, BrigadierArguments.greedyString().type)
        for (argument in listOf(BrigadierArguments.long(1, 3), BrigadierArguments.double(1.0, 3.0), BrigadierArguments.float(1f, 3f))) {
            for (value in listOf("0", "4")) assertFailsWith<CommandSyntaxException> { argument.parse(StringReader(value)) }
        }
        assertFailsWith<CommandSyntaxException> { BrigadierArguments.bool().parse(StringReader("yes")) }
    }

    @Test
    fun `wrong or absent argument lookups fail clearly`() {
        registration.register("lookup") {
            description = "Test"
            argCallback("number", BrigadierArguments.int()) {
                assertFailsWith<IllegalArgumentException> { getArgByName<String>("number") }
                assertFailsWith<IllegalArgumentException> { getArg(BrigadierArgument.of<Int>("missing")) }
            }
        }
        dispatcher.execute("lookup 1", source)
    }
}
