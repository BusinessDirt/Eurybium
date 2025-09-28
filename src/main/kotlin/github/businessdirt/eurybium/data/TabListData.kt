package github.businessdirt.eurybium.data

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.commands.brigadier.BrigadierArguments
import github.businessdirt.eurybium.core.comparators.PlayerListEntryComparator
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.modules.EurybiumModule
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.TabListUpdateEvent
import github.businessdirt.eurybium.events.minecraft.packet.PacketReceivedEvent
import github.businessdirt.eurybium.utils.StringUtils.stripLeadingAndTrailingColorResetFormatting
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket

@EurybiumModule
object TabListData {

    private var tabListCache: MutableList<String> = mutableListOf()

    /**
     * Reads the content of the tab list and returns a sorted list of the entries
     * @return a sorted list of every line on the tab list
     */
    private fun readTabList(): MutableList<String> {
        if (getMinecraft().networkHandler == null) return mutableListOf()

        // Get all listed entries, sort them and then limit them to 80,
        // which is the maximum number of players that minecraft's tab list can display
        val playerListEntries = getMinecraft().networkHandler!!.listedPlayerListEntries
            .sortedWith(PlayerListEntryComparator())
            .take(80)

        return playerListEntries.map { entry ->
            getMinecraft().inGameHud.playerListHud.getPlayerName(entry).string
                .stripLeadingAndTrailingColorResetFormatting()
                .trim()
        }.toMutableList()
    }

    private fun copyCommand(noColor: Boolean) {
        tabListCache.forEach(EurybiumMod.logger::debug)
    }

    @HandleEvent()
    fun onPacketReceivedEvent(event: PacketReceivedEvent) {
        if (event.packet !is PlayerListS2CPacket) return

        val tabList: MutableList<String> = readTabList()
        if (tabList.isEmpty()) return

        if (tabListCache != tabList) {
            tabListCache = tabList
            TabListUpdateEvent(tabListCache).post()

            TabWidget.onTabListUpdate(tabList)
        }
    }

    @HandleEvent()
    fun onCommandRegistrationEvent(event: CommandRegistrationEvent) {
        event.register("eurybiumlogtablist") {
            description = "Logs the tab list data to the console"
            category = CommandCategory.DEVELOPER_DEBUG
            simpleCallback { copyCommand(false) }
            argCallback("nocolor", BrigadierArguments.bool()) { noColor -> copyCommand(noColor) }
        }
    }
}
