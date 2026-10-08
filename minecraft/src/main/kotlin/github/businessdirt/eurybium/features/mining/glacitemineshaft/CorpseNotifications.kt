package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.events.skyblock.GlaciteMineshaftDetectionEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object CorpseNotifications {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.corpseNotifications

    @HandleEvent
    private fun onGlaciteMineshaftDetectionEvent(event: GlaciteMineshaftDetectionEvent) {
        if(!config.enabled) return
    }
}
