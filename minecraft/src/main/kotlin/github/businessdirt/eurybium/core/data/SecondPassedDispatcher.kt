package github.businessdirt.eurybium.core.data

import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.utils.SkyBlockUtils
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.events.minecraft.TickEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object SecondPassedDispatcher {
    private var nextSecond = 0L
    private var totalSeconds = 0

    @HandleEvent
    fun onTick(event: TickEvent) {
        if (!SkyBlockUtils.onHypixel()) { nextSecond = 0; return }
        val now = System.nanoTime()
        if (now >= nextSecond) {
            nextSecond = now + 1_000_000_000L
            SecondPassedEvent(totalSeconds++).post()
        }
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) { nextSecond = 0 }
}
