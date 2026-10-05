package github.businessdirt.eurybium

import github.businessdirt.eurybium.config.EurybiumConfig
import github.businessdirt.eurybium.config.manager.ConfigManager
import github.businessdirt.eurybium.core.commands.CommandCategory
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.*
import github.businessdirt.eurybium.processors.EurybiumModule
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@EurybiumModule
object EurybiumMod {

    val logger: Logger = LogManager.getLogger(EurybiumMod::class.java)

    lateinit var configManager: ConfigManager
    var config: EurybiumConfig = EurybiumConfig()

    @HandleEvent(eventType = PreModInitializationEvent::class, priority = Int.MIN_VALUE)
    private fun onPreModInitializationEvent() {
        //logger.initialize(EurybiumMod::class.java, config.dev.debug::enabled)
    }

    @HandleEvent(eventType = ModInitializationEvent::class, priority = Int.MIN_VALUE)
    private fun onModInitializationEvent() {
        configManager = ConfigManager()
        configManager.initialize()
    }

    @HandleEvent(eventType = PostModInitializationEvent::class, priority = Int.MIN_VALUE)
    private fun onPostModInitializationEvent() {

    }

    @HandleEvent(eventType = ModShutdownEvent::class, priority = Int.MAX_VALUE)
    private fun onModShutdownEvent() {

    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("eurybium") {
            category = CommandCategory.MAIN
            aliases = mutableListOf("eyb")
            description = "Opens the main Eurybium config"
            simpleCallback(configManager::openConfigGui)
        }
    }
}
