package github.businessdirt.eurybium

import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.config.EurybiumConfig
import github.businessdirt.eurybium.config.OrderedWaypointsRoutes
import github.businessdirt.eurybium.config.manager.ConfigManager
import github.businessdirt.eurybium.events.*
import github.businessdirt.eurybium.processors.EurybiumModule
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@EurybiumModule
object EurybiumMod {

    val logger: Logger = LogManager.getLogger(EurybiumMod::class.java)

    lateinit var configManager: ConfigManager
    var config: EurybiumConfig = EurybiumConfig()
    var orderedWaypointsRoutes: OrderedWaypointsRoutes = OrderedWaypointsRoutes()

    @HandleEvent(events = [ PreModInitializationEvent::class ], priority = Int.MIN_VALUE)
    private fun onPreModInitializationEvent() {

    }

    @HandleEvent(events = [ ModInitializationEvent::class ], priority = Int.MIN_VALUE)
    private fun onModInitializationEvent() {
        configManager = ConfigManager()
        configManager.initialize()
    }

    @HandleEvent(events = [ PostModInitializationEvent::class ], priority = Int.MIN_VALUE)
    private fun onPostModInitializationEvent() {

    }

    @HandleEvent(events = [ ModShutdownEvent::class ], priority = Int.MAX_VALUE)
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
