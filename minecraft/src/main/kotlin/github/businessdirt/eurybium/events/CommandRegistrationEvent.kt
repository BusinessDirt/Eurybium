package github.businessdirt.eurybium.events

import com.mojang.brigadier.CommandDispatcher
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierRootBuilder
import github.businessdirt.eurybium.api.commands.brigadier.CommandData
import github.businessdirt.eurybium.api.events.EurybiumEvent
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

class CommandRegistrationEvent(val dispatcher: CommandDispatcher<FabricClientCommandSource>) : EurybiumEvent() {
    private val builders: MutableList<CommandData> = mutableListOf()
    val commands: List<CommandData> get() = builders

    fun register(name: String, block: BrigadierRootBuilder<FabricClientCommandSource>.() -> Unit) {
        val rootCommand = BrigadierRootBuilder<FabricClientCommandSource>(name).apply(block)
        rootCommand.hasUniqueName(builders)
        rootCommand.checkDescriptionAndCategory()
        rootCommand.addToRegister(dispatcher, builders)
    }
}
