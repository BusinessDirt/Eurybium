package github.businessdirt.eurybium.commands

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.modules.EurybiumModule
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.utils.PostModInitializationEvent
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

@EurybiumModule
object CommandRegistry {
    @HandleEvent
    fun onPostModInitializationEvent(event: PostModInitializationEvent) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            CommandRegistrationEvent(dispatcher).post()
        }
    }
}
