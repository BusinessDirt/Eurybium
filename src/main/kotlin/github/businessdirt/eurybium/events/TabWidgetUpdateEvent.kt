package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.core.events.EurybiumEvent
import github.businessdirt.eurybium.data.model.TabWidget

class TabWidgetUpdateEvent(val widget: TabWidget, val lines: List<String>) : EurybiumEvent()
