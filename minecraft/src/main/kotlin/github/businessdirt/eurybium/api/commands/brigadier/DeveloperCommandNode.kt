package github.businessdirt.eurybium.api.commands.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.CommandContextBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import java.util.concurrent.CompletableFuture

/**
 * Keep the node registered even while disabled: Fabric copies the tree on connection.
 * A parse/suggestion gate survives that copy and takes effect without reconnecting.
 */
internal class DeveloperCommandNode<S>(
    original: LiteralCommandNode<S>,
    private val enabled: () -> Boolean,
) : LiteralCommandNode<S>(
    original.literal, original.command, original.requirement,
    original.redirect, original.redirectModifier, original.isFork,
) {
    init {
        original.children.forEach(::addChild)
    }

    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>) {
        if (!enabled()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(reader)
        super.parse(reader, contextBuilder)
    }

    override fun listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> =
        if (enabled()) super.listSuggestions(context, builder) else Suggestions.empty()

    override fun createBuilder(): LiteralArgumentBuilder<S> {
        // Fabric rebuilds each node and replaces its requirement and executor.
        // Preserve the gate in the rebuilt node as well as the execution tree.
        return object : LiteralArgumentBuilder<S>(literal) {
            override fun build(): LiteralCommandNode<S> = DeveloperCommandNode(super.build(), enabled)
        }.also {
            it.requires(requirement)
            it.executes(command)
            it.forward(redirect, redirectModifier, isFork)
        }
    }
}

/** Recheck at execution too, in case a previously parsed command outlives a config toggle. */
internal fun <S> guardExecutors(node: CommandNode<S>, enabled: () -> Boolean): CommandNode<S> {
    val builder = node.createBuilder()
    node.command?.let { command ->
        builder.executes { context ->
            if (!enabled()) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()
            command.run(context)
        }
    }
    node.children.forEach { builder.then(guardExecutors(it, enabled)) }
    return builder.build()
}
