package github.businessdirt.eurybium.api.commands.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.commands.CommandCategory
import net.minecraft.commands.SharedSuggestionProvider

class BrigadierRootBuilder<S : SharedSuggestionProvider>(override val name: String) : CommandData, BrigadierBuilder<S, LiteralArgumentBuilder<S>>(
    LiteralArgumentBuilder.literal(name)
) {
    override var description: String = ""
    override var category: CommandCategory = CommandCategory.MAIN
    override var aliases: MutableList<String> = mutableListOf()

    var node: CommandNode<S>? = null

    fun addToRegister(dispatcher: CommandDispatcher<S>, builders: MutableList<CommandData>) {
        val developerCommand = category in CommandCategory.developmentCategories
        val enabled = { EurybiumMod.config.dev.devCommands }
        val built = (this.builder as LiteralArgumentBuilder<S>).build()
        val original = if (developerCommand) {
            DeveloperCommandNode(guardExecutors(built, enabled) as LiteralCommandNode<S>, enabled)
        } else built
        dispatcher.root.addChild(original)
        this.node = original
        aliases.forEach {
            val alias = LiteralArgumentBuilder.literal<S>(it).redirect(original).executes(original.command).build()
            dispatcher.root.addChild(if (developerCommand) DeveloperCommandNode(alias, enabled) else alias)
        }

        this.addBuilder(builders)
    }
}
