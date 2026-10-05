package github.businessdirt.eurybium

import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.events.HandleEvent.Companion.HIGHEST
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.ModInitializationEvent
import github.businessdirt.eurybium.events.PostModInitializationEvent
import github.businessdirt.eurybium.events.PreModInitializationEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object EurybiumMod {

    @HandleEvent(eventType = PreModInitializationEvent::class, priority = HIGHEST)
    private fun onPreModInitializationEvent() {
        //logger.initialize(EurybiumMod::class.java, config.dev.debug::enabled)
    }

    @HandleEvent(eventType = ModInitializationEvent::class, priority = HIGHEST)
    private fun onModInitializationEvent() {
        //SecondPassedEvent.schedule()
    }

    @HandleEvent(eventType = PostModInitializationEvent::class, priority = HIGHEST)
    private fun onPostModInitializationEvent() {

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
