package github.businessdirt.eurybium.core.utils

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.TickEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object SecondPassedDispatcher {

    private var nextSecond = 0L
    private var totalSeconds = 0

    @HandleEvent(eventType = TickEvent::class)
    private fun onTickEvent() {
        if (!SkyBlockUtils.onHypixel()) { nextSecond = 0; return }
        val now = System.nanoTime()
        if (now >= nextSecond) {
            nextSecond = now + 1_000_000_000L
            SecondPassedEvent(totalSeconds++).post()
        }
    }

    @HandleEvent(eventType = ClientDisconnectEvent::class)
    private fun onClientDisconnectEvent() { nextSecond = 0 }
}
