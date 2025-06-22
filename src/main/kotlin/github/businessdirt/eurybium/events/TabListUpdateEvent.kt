package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.core.events.EurybiumEvent

class TabListUpdateEvent(val tabList: List<String>) : EurybiumEvent()
