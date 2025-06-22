package github.businessdirt.eurybium.commands.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import github.businessdirt.eurybium.commands.CommandCategory
import net.minecraft.command.CommandSource
import java.util.function.Consumer

class BrigadierRootBuilder<S : CommandSource>(override val name: String) : CommandData, BrigadierBuilder<S, LiteralArgumentBuilder<S>>(
    LiteralArgumentBuilder.literal(name)
) {
    override var description: String = ""
    override var category: CommandCategory = CommandCategory.MAIN
    override var aliases: MutableList<String> = mutableListOf()

    var node: CommandNode<S>? = null

    fun addToRegister(dispatcher: CommandDispatcher<S>, builders: MutableList<CommandData>) {
        val original = dispatcher.register(this.builder as LiteralArgumentBuilder<S>)
        this.node = original
        aliases.forEach {
            dispatcher.register(LiteralArgumentBuilder.literal<S>(it).redirect(original).executes(original.getCommand()))
        }

        this.addBuilder(builders)
    }
}
