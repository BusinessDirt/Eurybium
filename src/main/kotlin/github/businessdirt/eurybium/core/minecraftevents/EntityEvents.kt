package github.businessdirt.eurybium.core.minecraftevents

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.modules.EurybiumModule
import github.businessdirt.eurybium.events.utils.PreModInitializationEvent

@EurybiumModule
object EntityEvents {

    @HandleEvent()
    fun register(event: PreModInitializationEvent) {
    }
}
