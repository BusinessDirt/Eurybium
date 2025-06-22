package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.core.events.EurybiumEvent

class TickEvent(val tick: Long) : EurybiumEvent() {
    fun isMod(i: Int, offset: Int): Boolean = (tick + offset) % i == 0L
}
