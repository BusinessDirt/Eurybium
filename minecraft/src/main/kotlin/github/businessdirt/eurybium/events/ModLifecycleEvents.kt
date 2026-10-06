package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.api.events.EurybiumEvent

object PreModInitializationEvent : EurybiumEvent()
object ModInitializationEvent : EurybiumEvent()
object PostModInitializationEvent : EurybiumEvent()
object ModShutdownEvent : EurybiumEvent()

class SecondPassedEvent(private val totalSeconds: Int) : EurybiumEvent() {
    fun repeatSeconds(i: Int): Boolean = totalSeconds % i == 0
}

