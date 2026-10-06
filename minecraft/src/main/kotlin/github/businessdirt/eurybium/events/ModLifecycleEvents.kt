package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.api.events.EurybiumEvent

object PreModInitializationEvent : EurybiumEvent()
object ModInitializationEvent : EurybiumEvent()
object PostModInitializationEvent : EurybiumEvent()
object ModShutdownEvent : EurybiumEvent()

