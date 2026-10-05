package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.api.events.EurybiumEvent

@Suppress("unused")
class TabListUpdateEvent(val tabList: List<String>) : EurybiumEvent()
