package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent

object ViableMineshaft {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft
    private var corpseCount: Int = 0

    @HandleEvent()
    fun onTabWidgetUpdateEvent(event: TabWidgetUpdateEvent) {
        if (event.widget == TabWidget.FROZEN_CORPSES) {
            event.widget.lines.forEach { EurybiumMod.logger.debug(it) }
        }
    }
}
