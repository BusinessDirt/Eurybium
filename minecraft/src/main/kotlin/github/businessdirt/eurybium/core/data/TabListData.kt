package github.businessdirt.eurybium.core.data

import github.businessdirt.eurybium.core.data.model.TabWidget
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.utils.ComponentUtils.legacyString
import github.businessdirt.eurybium.core.utils.StringUtils.stripLeadingAndTrailingColorResetFormatting
import github.businessdirt.eurybium.events.TabListUpdateEvent
import github.businessdirt.eurybium.events.minecraft.*
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.world.level.GameType

@EurybiumModule
object TabListData {
    var tabListCache: List<String> = emptyList()
        private set

    @HandleEvent(eventType = TickEvent::class, priority = HandleEvent.HIGH)
    private fun onTickEvent() {
        val client = Minecraft.getInstance()
        val entries = client.connection?.listedOnlinePlayers.orEmpty()
            .sortedWith(compareByDescending<PlayerInfo> { it.tabListOrder }
                .thenBy { it.gameMode == GameType.SPECTATOR }
                .thenBy { it.team?.name.orEmpty() }
                .thenBy { it.profile.name().lowercase() })
            .take(80)

        val lines = entries.map {
            client.gui.tabList.getNameForDisplay(it).legacyString()
                .stripLeadingAndTrailingColorResetFormatting().trim()
        }

        if (lines != tabListCache) {
            tabListCache = lines
            TabListUpdateEvent(lines).post()
        }

        // Re-evaluate widgets when SkyBlock state changes even if tab lines stay identical.
        TabWidget.onTabListUpdate(lines)
    }

    @HandleEvent(eventTypes = [ WorldChangeEvent::class, ClientDisconnectEvent::class ])
    private fun onTabListClearEvents() = clear()

    private fun clear() {
        val changed = tabListCache.isNotEmpty()
        tabListCache = emptyList()

        if (changed) TabListUpdateEvent(emptyList()).post()

        TabWidget.onTabListUpdate(emptyList())
    }
}
