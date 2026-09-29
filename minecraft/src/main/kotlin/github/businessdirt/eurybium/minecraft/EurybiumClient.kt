package github.businessdirt.eurybium.minecraft

import github.businessdirt.eurybium.core.Eurybium
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents

class EurybiumClient : ClientModInitializer {
    override fun onInitializeClient() {
        val core = Eurybium(FabricClientPlatform())
        core.start()
        MinecraftHooks.attach(core)

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(literal("eurybium").executes {
                core.showStatus()
                1
            })
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            MinecraftHooks.detach()
            core.stop()
        }
    }
}
