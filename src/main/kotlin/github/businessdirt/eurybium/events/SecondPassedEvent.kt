package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.core.events.EurybiumEvent
import github.businessdirt.eurybium.utils.Reference
import github.businessdirt.eurybium.utils.Scheduler
import github.businessdirt.eurybium.utils.SkyBlockUtils
import kotlin.concurrent.fixedRateTimer

class SecondPassedEvent(private val totalSeconds: Int) : EurybiumEvent() {
    fun repeatSeconds(i: Int): Boolean = this.totalSeconds % i == 0

    companion object {
        private var totalSeconds = 0

        fun schedule() {
            fixedRateTimer(name = "${Reference.MOD_ID}-second-passed-event-fixed-rate-timer", period = 1000L) {
                Scheduler.onThread.execute {
                    if (!SkyBlockUtils.onHypixel()) return@execute
                    SecondPassedEvent(totalSeconds).post()
                    totalSeconds++
                }
            }
        }
    }
}
