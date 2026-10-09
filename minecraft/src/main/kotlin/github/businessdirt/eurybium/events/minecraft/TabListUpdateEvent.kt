package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.api.events.EurybiumEvent
import net.minecraft.network.chat.Component

/** Displayed tab-list entries changed, including ordering, styles, additions, and removals. */
class TabListUpdateEvent(
    val newLines: List<Component>,
    val oldLines: List<Component>,
) : EurybiumEvent()
