package github.businessdirt.eurybium

import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.core.events.EurybiumEventBus
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.modules.LoadedModules
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.ModInitializationEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

@EurybiumModule
object EurybiumMod {

    /**
     * Runs before the [github.businessdirt.eurybium.core.events.EurybiumEventBus] is initialized.
     */
    fun preInit() {
        EurybiumEventBus.init(LoadedModules.modules)

        Configurator.setLevel("com.mojang.authlib.yggdrasil", Level.FATAL)
        //logger.initialize(EurybiumMod::class.java, config.dev.debug::enabled)
    }

    @HandleEvent
    fun onPreModInitializationEvent(event: ModInitializationEvent) {
        //logger.initialize(EurybiumMod::class.java, config.dev.debug::enabled)
    }

    @HandleEvent
    fun onModInitializationEvent(event: ModInitializationEvent) {
        //SecondPassedEvent.schedule()
    }

    @HandleEvent
    fun onPostModInitializationEvent(event: ModInitializationEvent) {

    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("eurybium") {
            category = CommandCategory.MAIN
            aliases = mutableListOf("eyb")
            description = "Opens the main Eurybium config"
            simpleCallback {
                //configManager.openConfigGui()
            }
        }
    }
}
