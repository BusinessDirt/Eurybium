package github.businessdirt.eurybium

import github.businessdirt.eurybium.core.events.EurybiumEventBus
import github.businessdirt.eurybium.core.modules.LoadedModules
import github.businessdirt.eurybium.events.ModInitializationEvent
import github.businessdirt.eurybium.events.ModShutdownEvent
import github.businessdirt.eurybium.events.PostModInitializationEvent
import github.businessdirt.eurybium.events.PreModInitializationEvent
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

@Suppress("unused")
class EurybiumModLoader : ClientModInitializer {

    override fun onInitializeClient() {
        EurybiumEventBus.init(LoadedModules.modules)
        Configurator.setLevel("com.mojang.authlib.yggdrasil", Level.FATAL)

        PreModInitializationEvent.post()
        ModInitializationEvent.post()
        PostModInitializationEvent.post()

        ClientLifecycleEvents.CLIENT_STOPPING.register { ModShutdownEvent.post() }
    }
}
