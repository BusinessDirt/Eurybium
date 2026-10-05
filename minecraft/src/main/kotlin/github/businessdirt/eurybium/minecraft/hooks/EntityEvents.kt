package github.businessdirt.eurybium.minecraft.hooks

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import github.businessdirt.eurybium.events.PreModInitializationEvent

@EurybiumModule
object EntityEvents {

    @HandleEvent(eventType = PreModInitializationEvent::class)
    private fun onPreModInitializationEvent() {

    }
}
