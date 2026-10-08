package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.events.skyblock.GlaciteMineshaftDetectionEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object CorpseNotifications {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.corpseNotifications

    private var miningFiestaActive = false

    @HandleEvent
    private fun onGlaciteMineshaftDetectionEvent(event: GlaciteMineshaftDetectionEvent) {
        if(!config.enabled) return

        val thresholds = if (miningFiestaActive) config.fiestaCorpseThresholds else config.corpseThresholds
        val minLapisCorpseCount: Int = when(event.type) {
            MineshaftType.TOPA_1, MineshaftType.TOPA_2 -> thresholds.topaz
            MineshaftType.SAPP_1, MineshaftType.SAPP_2 -> thresholds.sapphire
            MineshaftType.AMET_1, MineshaftType.AMET_2 -> thresholds.amethyst
            MineshaftType.AMBE_1, MineshaftType.AMBE_2 -> thresholds.amber
            MineshaftType.JADE_1, MineshaftType.JADE_2 -> thresholds.jade
            MineshaftType.JASP_1, MineshaftType.JASP_C -> thresholds.jasper
            MineshaftType.OPAL_1, MineshaftType.OPAL_C -> thresholds.opal
            MineshaftType.RUBY_1, MineshaftType.RUBY_2, MineshaftType.RUBY_C -> thresholds.ruby
            MineshaftType.ONYX_1, MineshaftType.ONYX_2, MineshaftType.ONYX_C -> thresholds.onyx
            MineshaftType.AQUA_1, MineshaftType.AQUA_2, MineshaftType.AQUA_C, MineshaftType.LITT_L -> thresholds.aquamarine
            MineshaftType.CITR_1,  MineshaftType.CITR_2, MineshaftType.CITR_C -> thresholds.citrine
            MineshaftType.PERI_1, MineshaftType.PERI_2, MineshaftType.PERI_C -> thresholds.peridot
            MineshaftType.FAIR_1 -> -1
            MineshaftType.TITA_1, MineshaftType.UMBE_1, MineshaftType.TUNG_1 -> {
                ChatAPI.chat("Loot Lapis Corpses and leave.")
                -1
            }
        }.toInt()

        if (minLapisCorpseCount < 0) return

        // TODO: read corpse count from tab list and compare then send message
    }
}
