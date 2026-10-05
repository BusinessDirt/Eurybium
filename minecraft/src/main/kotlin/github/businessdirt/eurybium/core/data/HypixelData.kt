package github.businessdirt.eurybium.core.data

import github.businessdirt.eurybium.core.data.model.IslandType
import github.businessdirt.eurybium.core.data.model.TabWidget
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import github.businessdirt.eurybium.core.utils.StringUtils.removeColor
import github.businessdirt.eurybium.events.ScoreboardUpdateEvent
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.events.hypixel.*
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.ClientJoinEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardTitleUpdateEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.client.Minecraft
import java.util.*

@EurybiumModule
object HypixelData {

    var hypixelLive = false
        private set

    var hypixelAlpha = false
        private set

    var skyBlock = false
        private set

    var skyBlockIsland = IslandType.NONE
        private set

    var joinedWorld = SimpleTimeMark.farPast()
        private set

    var skyBlockArea = ""
        private set

    var skyBlockAreaWithSymbol = ""
        private set

    val connectedToHypixel get() = hypixelLive || hypixelAlpha
    private val titlePattern = Regex("SK[YI]BLOCK(?: CO-OP| GUEST)?")
    private val areaPattern = Regex("\\s*[⏣ф] (?<area>.*)")

    @HandleEvent(eventType = ClientJoinEvent::class, priority = HandleEvent.HIGHEST)
    private fun onClientJoinEvent() {
        val host = Minecraft.getInstance().currentServer?.ip?.substringBefore(':')
            ?.lowercase(Locale.ROOT).orEmpty()

        val wasConnected = connectedToHypixel
        val isHypixel = host == "hypixel.net" || host.endsWith(".hypixel.net")

        hypixelAlpha = isHypixel && host == "alpha.hypixel.net"
        hypixelLive = isHypixel && !hypixelAlpha

        if (!wasConnected && connectedToHypixel) HypixelJoinEvent().post()
        if (wasConnected && !connectedToHypixel) { leaveSkyBlock(); HypixelLeaveEvent().post() }
    }

    @HandleEvent(eventType = ClientDisconnectEvent::class, priority = HandleEvent.HIGHEST)
    private fun onClientDisconnectEvent() {
        val wasConnected = connectedToHypixel

        leaveSkyBlock()

        hypixelLive = false
        hypixelAlpha = false

        if (wasConnected) HypixelLeaveEvent().post()
    }

    @HandleEvent(eventType = WorldChangeEvent::class, priority = HandleEvent.HIGH)
    private fun onWorldChangeEvent() {
        joinedWorld = SimpleTimeMark.now()
        leaveSkyBlock()
    }

    @HandleEvent(eventType = ScoreboardTitleUpdateEvent::class)
    private fun onScoreboardTitleUpdateEvent() = updateSkyBlock()

    @HandleEvent
    private fun onScoreboardUpdateEvent(event: ScoreboardUpdateEvent) {
        updateSkyBlock()

        if (!skyBlock) return

        val line = event.newLines.firstOrNull { areaPattern.containsMatchIn(it) } ?: return
        val area = areaPattern.find(line)?.groups?.get("area")?.value?.removeColor() ?: return
        skyBlockAreaWithSymbol = line.trim()

        if (area != skyBlockArea) {
            val previous = skyBlockArea
            skyBlockArea = area
            ScoreboardAreaChangedEvent(previous, area).post()
        }
    }

    @HandleEvent
    private fun onTabWidgetUpdateEvent(event: TabWidgetUpdateEvent) {
        if (event.widget != TabWidget.AREA || !skyBlock) return

        val name = TabWidget.AREA.matchFirstLine { group("island").removeColor() } ?: return
        var island = IslandType.getByName(name) ?: IslandType.UNKNOWN

        if (ScoreboardData.objectiveTitle.contains("GUEST")) island = island.guestVariant()

        setIsland(island)
    }

    private fun updateSkyBlock() {
        val next = connectedToHypixel && titlePattern.containsMatchIn(ScoreboardData.objectiveTitle.removeColor())

        if (next == skyBlock) return

        if (next) {
            skyBlock = true
            SkyblockJoinEvent().post()
        } else leaveSkyBlock()
    }

    private fun setIsland(next: IslandType) {
        if (next == skyBlockIsland) return

        val previous = skyBlockIsland
        skyBlockIsland = next

        SkyblockIslandChangeEvent(previous, next).post()
    }

    private fun leaveSkyBlock() {
        val wasSkyBlock = skyBlock
        skyBlock = false
        skyBlockArea = ""
        skyBlockAreaWithSymbol = ""
        setIsland(IslandType.NONE)

        if (wasSkyBlock) SkyblockLeaveEvent().post()
    }
}
