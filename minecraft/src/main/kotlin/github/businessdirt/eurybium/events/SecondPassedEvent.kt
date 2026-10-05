package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.api.events.EurybiumEvent

class SecondPassedEvent(private val totalSeconds: Int) : EurybiumEvent() {
    fun repeatSeconds(i: Int): Boolean = totalSeconds % i == 0
}
