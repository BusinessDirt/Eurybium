package github.businessdirt.eurybium.core.minecraftevents

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import github.businessdirt.eurybium.events.PreModInitializationEvent

@EurybiumModule
object EntityEvents {

    @HandleEvent
    fun register(event: PreModInitializationEvent) {
    }
}
