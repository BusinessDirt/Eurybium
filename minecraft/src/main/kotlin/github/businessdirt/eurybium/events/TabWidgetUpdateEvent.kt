package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.data.model.TabWidget

@Suppress("unused")
class TabWidgetUpdateEvent(val widget: TabWidget, val lines: List<String>) : EurybiumEvent()
