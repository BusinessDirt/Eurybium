package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.TabListAPI
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import github.businessdirt.eurybium.api.repo.RepoPattern
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.events.RepoUpdateEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.TabListUpdateEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.skyblock.GlaciteMineshaftDetectionEvent
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object CorpseNotifications {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.corpseNotifications

    private val frozenCorpsesPattern = RepoPattern("tab.widget.frozen_corpses", """\s*(?:Frozen Corpses:)""")
    private var miningFiestaActive = false
    private var pendingThreshold: Int? = null

    @HandleEvent
    private fun onGlaciteMineshaftDetectionEvent(event: GlaciteMineshaftDetectionEvent) {
        pendingThreshold = null
        if (!config.enabled) return

        val thresholds = if (miningFiestaActive) config.fiestaCorpseThresholds else config.corpseThresholds
        val minLapisCorpseCount: Int = when (event.type) {
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
            MineshaftType.CITR_1, MineshaftType.CITR_2, MineshaftType.CITR_C -> thresholds.citrine
            MineshaftType.PERI_1, MineshaftType.PERI_2, MineshaftType.PERI_C -> thresholds.peridot
            MineshaftType.FAIR_1 -> -1
            MineshaftType.TITA_1, MineshaftType.UMBE_1, MineshaftType.TUNG_1 -> {
                ChatAPI.chat("Loot Lapis Corpses and leave.")
                -1
            }
        }.toInt()

        // Five is the configured disabled value, rather than an attainable corpse threshold.
        if (minLapisCorpseCount !in 0..4) return
        pendingThreshold = minLapisCorpseCount
        checkCorpses()
    }

    @HandleEvent(events = [ TabListUpdateEvent::class, RepoUpdateEvent::class ])
    private fun onCorpseDataUpdateEvents() = checkCorpses()

    @HandleEvent(events = [ WorldChangeEvent::class, ClientDisconnectEvent::class ])
    private fun onWorldClearEvents() { pendingThreshold = null }

    private fun checkCorpses() {
        val count = takeNotificationCount(TabListAPI.linesPlain) ?: return
        ChatAPI.chat("This mineshaft has $count Lapis Corpses, meeting your corpse threshold.")
    }

    /** Remain pending until the widget arrives and the threshold is reached; then notify only once. */
    internal fun takeNotificationCount(
        lines: List<String>,
        pattern: Regex? = frozenCorpsesPattern.resolve(),
    ): Int? {
        val threshold = pendingThreshold ?: return null
        if (!config.enabled) return null

        val count = lapisCorpseCount(lines, pattern) ?: return null
        if (count < threshold) return null

        pendingThreshold = null

        return count
    }

    /** Null means the widget is unavailable, which must not satisfy a zero-corpse threshold. */
    internal fun lapisCorpseCount(lines: List<String>, pattern: Regex?): Int? {
        if (pattern == null) return null

        val plain = lines.map { it.removeColor().trimEnd() }
        val headerIndex = plain.indexOfFirst(pattern::matches)
        if (headerIndex < 0) return null

        return plain.asSequence().drop(headerIndex + 1)
            .takeWhile { it.isNotBlank() }
            .count { it.trimStart().startsWith("Lapis:") }
    }
}
