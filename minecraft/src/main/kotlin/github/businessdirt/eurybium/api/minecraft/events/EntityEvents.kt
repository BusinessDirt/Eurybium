package github.businessdirt.eurybium.api.minecraft.events

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import github.businessdirt.eurybium.events.PreModInitializationEvent

@EurybiumModule
object EntityEvents {

    @HandleEvent(eventType = PreModInitializationEvent::class)
    private fun onPreModInitializationEvent() {

    }
}
