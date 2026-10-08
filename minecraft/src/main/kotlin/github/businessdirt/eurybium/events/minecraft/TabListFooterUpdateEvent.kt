package github.businessdirt.eurybium.events.minecraft

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.legacyString
import net.minecraft.network.chat.Component

/** Footer changed independently of the entries; null signals that it was cleared. */
class TabListFooterUpdateEvent(
    val newFooter: Component?,
    val oldFooter: Component?,
) : EurybiumEvent() {
    val lines: List<String> = newFooter?.legacyString()?.split('\n').orEmpty()
}
