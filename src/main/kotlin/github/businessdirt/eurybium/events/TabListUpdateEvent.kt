package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.core.events.EurybiumEvent

@Suppress("unused")
class TabListUpdateEvent(val tabList: List<String>) : EurybiumEvent()
