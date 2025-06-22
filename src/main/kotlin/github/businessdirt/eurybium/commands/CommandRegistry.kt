package github.businessdirt.eurybium.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import github.businessdirt.eurybium.commands.brigadier.BrigadierRootBuilder
import github.businessdirt.eurybium.commands.brigadier.CommandData
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.utils.PostModInitializationEvent
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Consumer

object CommandRegistry {
    @HandleEvent
    fun onPostModInitializationEvent(event: PostModInitializationEvent) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            CommandRegistrationEvent(dispatcher).post()
        }
    }
}
