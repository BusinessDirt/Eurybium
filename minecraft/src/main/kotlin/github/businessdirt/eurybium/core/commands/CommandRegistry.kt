package github.businessdirt.eurybium.core.commands

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.PostModInitializationEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

@EurybiumModule
object CommandRegistry {

    @HandleEvent
    fun onPostModInitializationEvent(event: PostModInitializationEvent) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            CommandRegistrationEvent(dispatcher).post()
        }
    }
}
