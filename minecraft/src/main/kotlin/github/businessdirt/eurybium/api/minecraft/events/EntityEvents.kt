package github.businessdirt.eurybium.api.minecraft.events

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.events.PreModInitializationEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object EntityEvents {

    @HandleEvent(events = [ PreModInitializationEvent::class ])
    private fun onPreModInitializationEvent() {

    }
}
