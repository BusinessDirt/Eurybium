package github.businessdirt.eurybium.data

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.logging.Chat
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.ScoreboardUpdateEvent
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.ClientJoinEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.skyblock.ScoreboardAreaChangedEvent
import github.businessdirt.eurybium.events.skyblock.SkyblockIslandChangeEvent
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import github.businessdirt.eurybium.utils.StringUtils.removeColor
import java.util.regex.Matcher
import java.util.regex.Pattern

object HypixelData {
    val SERVER_NAME_CONNECTION_PATTERN: Pattern = Pattern.compile("(?<prefix>.+\\.)?hypixel\\.net")
    val ISLAND_NAME_PATTERN: Pattern = Pattern.compile("(?:§.)*(?:Area|Dungeon): (?:§.)*(?<island>.*)")
    val SCOREBOARD_TITLE_PATTERN: Pattern = Pattern.compile("SK[YI]BLOCK(?: CO-OP| GUEST)?")
    val SKYBLOCK_AREA_PATTERN: Pattern = Pattern.compile("\\s*(?<symbol>[⏣ф]) (?<area>.*)")
    val GUEST_PATTERN: Pattern = Pattern.compile("SKYBLOCK GUEST")

    var hypixelLive = false
    var hypixelAlpha = false
    var skyBlock = false
    var skyBlockIsland: IslandType = IslandType.UNKNOWN

    var joinedWorld: SimpleTimeMark = SimpleTimeMark.farPast()

    var skyBlockArea = ""
    var skyblockAreaWithSymbol = ""

    val connectedToHypixel get() = hypixelLive || hypixelAlpha

    @HandleEvent
    fun onWorldChangeEvent(event: WorldChangeEvent) {
        skyBlock = false
        joinedWorld = SimpleTimeMark.now()
        skyBlockArea = ""
        skyblockAreaWithSymbol = ""
    }

    @HandleEvent
    fun onClientDisconnectEvent(event: ClientDisconnectEvent) {
        hypixelLive = false
        hypixelAlpha = false
        skyBlock = false
        skyBlockArea = ""
        skyblockAreaWithSymbol = ""
    }

    @HandleEvent
    fun onClientJoinEvent(event: ClientJoinEvent) {
        val address = event.connection.address.toString()
        val matcher: Matcher = SERVER_NAME_CONNECTION_PATTERN.matcher(address)
        if (matcher.find()) {
            when {
                matcher.group("prefix") == "alpha." -> hypixelAlpha = true
                else -> hypixelLive = true
            }
        }
    }

    @HandleEvent
    fun onScoreboardUpdateEvent(event: ScoreboardUpdateEvent) {
        if (!connectedToHypixel) return

        val displayName = ScoreboardData.objectiveTitle.removeColor()
        skyBlock = SCOREBOARD_TITLE_PATTERN.matcher(displayName).find()
        if (!skyBlock) return

        for (line in event.added) {
            val matcher: Matcher = SKYBLOCK_AREA_PATTERN.matcher(line)
            if (!matcher.find()) continue

            val area = matcher.group("area").removeColor()
            skyblockAreaWithSymbol = line.trim()

            if (area != skyBlockArea) {
                val previousArea = skyBlockArea
                skyBlockArea = area
                ScoreboardAreaChangedEvent(previousArea, area).post()
            }

            break
        }
    }

    @HandleEvent
    fun onTabWidgetUpdateEvent(event: TabWidgetUpdateEvent) {
        if (event.widget != TabWidget.AREA) return

        val guesting: Boolean = GUEST_PATTERN.matcher(ScoreboardData.objectiveTitle).find()

        val foundIsland = TabWidget.AREA.matchFirstLine { group("island").removeColor() } ?: return
        Chat.chat(foundIsland)

        var newIsland = IslandType.getByName(foundIsland) ?: IslandType.UNKNOWN
        if (guesting) newIsland = newIsland.guestVariant()

        if (newIsland != skyBlockIsland) {
            val oldIsland: IslandType = skyBlockIsland
            skyBlockIsland = newIsland
            SkyblockIslandChangeEvent(oldIsland, newIsland).post()

            if (newIsland == IslandType.UNKNOWN) {
                EurybiumMod.logger.debug("Unknown island detected: '{}'", foundIsland)
            }
        }
    }
}
