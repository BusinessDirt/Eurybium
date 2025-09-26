package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.hypixel.MineshaftEnteredEvent
import github.businessdirt.eurybium.features.types.MineshaftType

object ViableMineshaftDetection {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.mineshaftMining
    private var mineshaftType: MineshaftType = MineshaftType.UNKNOWN

    @HandleEvent
    fun onTabWidgetUpdateEvent(event: TabWidgetUpdateEvent) {
        if (!config.detectViableMineshafts || mineshaftType == MineshaftType.UNKNOWN) return

        if (event.widget == TabWidget.FROZEN_CORPSES) {
            println(event.lines)
        }
    }

    @HandleEvent
    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {
        mineshaftType = event.type
    }

    @HandleEvent
    fun onWorldChangeEvent(event: WorldChangeEvent) {
        mineshaftType = MineshaftType.UNKNOWN
    }
}