package github.businessdirt.eurybium.commands.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider
import github.businessdirt.eurybium.utils.BrigadierUtils.isGreedy
import github.businessdirt.eurybium.utils.BrigadierUtils.toSuggestionProvider
import github.businessdirt.eurybium.utils.StringUtils.hasWhitespace
import github.businessdirt.eurybium.utils.StringUtils.splitFirstWhitespace
import github.businessdirt.eurybium.utils.StringUtils.splitLastWhitespace
import net.minecraft.command.CommandSource
import java.util.*

typealias LiteralCommandBuilder<S> = BrigadierBuilder<S, LiteralArgumentBuilder<S>>
typealias ArgumentCommandBuilder<S, T> = BrigadierBuilder<S, RequiredArgumentBuilder<S, T>>

/**
 * S is a CommandSource like [FabricClientCommandSource][net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource]
 * B is the concrete ArgumentBuilder type like [LiteralArgumentBuilder] or [RequiredArgumentBuilder]
 */
@Suppress("unused")
open class BrigadierBuilder<S : CommandSource, B : ArgumentBuilder<S, B>>(
    builder: ArgumentBuilder<S, B>,
    private val hasGreedyArg: Boolean = false
) {
    val builder: ArgumentBuilder<S, B> = Objects.requireNonNull<ArgumentBuilder<S, B>>(builder)

    private fun checkGreedy() {
        check(!hasGreedyArg) { "Cannot add an argument/literal to a builder that has a greedy argument." }
    }

    /**
     * Executes the code block when the command is executed.
     */
    fun callback(block: ArgContext.() -> Unit) {
        this.builder.executes {
            block(ArgContext(it))
            1
        }
    }

    /**
     * Alternative to [callback] when no arguments are needed.
     */
    fun simpleCallback(block: () -> Unit) {
        this.builder.executes {
            block()
            1
        }
    }

    /**
     * Adds one or more literals to the command.
     * If the given string contains multiple space-separated names,
     * they are interpreted as a chain of literals.
     *
     * For example, the following usage:
     * ```kt
     * literal("first second") {
     *    // do something
     * }
     * ```
     * Is the same as this usage:
     * ```kt
     * literal("first") {
     *    literal("second") {
     *       // do something
     *    }
     * }
     * ```
     */
    fun literal(vararg names: String, action: LiteralCommandBuilder<S>.() -> Unit) {
        checkGreedy()
        for (name in names) {
            if (name.hasWhitespace()) {
                val (literal, nextLiteral) = name.splitFirstWhitespace()
                this.literal(literal) {
                    this.literal(nextLiteral) {
                        action(this)
                    }
                }

                return
            }

            val child = BrigadierBuilder(LiteralArgumentBuilder.literal<S>(name))
            child.action()
            this.builder.then(child.builder)
        }
    }

    /**
     * Adds an argument to the command. If in the same string there are
     * different names separated by spaces, only the last name is used as
     * the name of the argument, and the previous ones are treated as literals.
     */
    inline fun <reified T> arg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        crossinline action: ArgumentCommandBuilder<S, T>.(BrigadierArgument<T>) -> Unit
    ) = arg(name, argument, suggestions.toSuggestionProvider(), action)

    inline fun <reified T> arg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<S>? = null,
        crossinline action: ArgumentCommandBuilder<S, T>.(BrigadierArgument<T>) -> Unit
    ) {
        if (!name.hasWhitespace()) {
            internalArg(name, argument, suggestions) { action(BrigadierArgument.of(name)) }
            return
        }
        val (literalNames, argName) = name.splitLastWhitespace()
        this.literal(literalNames) {
            internalArg(argName, argument, suggestions) { action(BrigadierArgument.of<T>(argName)) }
        }
    }

    /**
     * Internal method that actually creates the [RequiredArgumentBuilder] and attaches it.
     * The 'builder' consumer receives the created [BrigadierBuilder] for the Created [RequiredArgumentBuilder]
     * and the [<] instance for retrieving values in callbacks.
     */
    fun <T> internalArg(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<S>? = null,
        action: ArgumentCommandBuilder<S, T>.() -> Unit
    ) {
        checkGreedy()

        if (name.hasWhitespace()) {
            val (prevLiteral, nextLiteral) = name.splitLastWhitespace()
            this.literal(prevLiteral) {
                internalArg(nextLiteral, argument, suggestions, action)
            }

            return
        }

        val isGreedy = argument.isGreedy()
        val builder = BrigadierBuilder(RequiredArgumentBuilder.argument<S, T>(name, argument).apply {
                if (suggestions != null) suggests(suggestions)
            },
            isGreedy
        )
        builder.action()
        this.builder.then(builder.builder)
    }

    /**
     * This function allows for the usage of a callback within a literal without having to
     * create a block for each one.
     *
     * For example, this usage of literalCallback
     * ```kt
     * literalCallback("test") {
     *     // do something
     * }
     * ```
     * is the same as this usage of literal and callback separately:
     * ```kt
     * literal("test") {
     *     callback {
     *        // do something
     *     }
     * }
     * ```
     */
    fun literalCallback(vararg names: String, block: ArgContext.() -> Unit) = literal(*names) {callback(block)}

    /**
     * This function allows for the usage of a callback within an argument without having to
     * create a block for each one.
     *
     * However, differently from [literalCallback]
     * For args, the same applies; the only difference is that instead of giving a [BrigadierArgument] as a parameter,
     * it directly gives the value of the argument.
     *
     * For example, the following two usages are the same:
     *
     * ```kt
     * argCallback("input", BrigadierArguments.string()) { input ->
     *     ChatUtils.chat("Sent input: $input")
     * }
     * ```
     * ```kt
     * arg("input", BrigadierArguments.string()) { inputArg ->
     *    callback {
     *       val input = getArg(inputArg)
     *       ChatUtils.chat("Sent input: $input")
     *    }
     * }
     * ```
     */
    inline fun <reified T> argCallback(
        name: String,
        argument: ArgumentType<T>,
        suggestions: Collection<String>,
        crossinline block: ArgContext.(T) -> Unit
    ) = arg(name, argument, suggestions) { callback { block(getArg(it)) } }

    inline fun <reified T> argCallback(
        name: String,
        argument: ArgumentType<T>,
        suggestions: SuggestionProvider<S>? = null,
        crossinline block: ArgContext.(T) -> Unit
    ) = arg(name, argument, suggestions) { callback { block(getArg(it)) } }
}
