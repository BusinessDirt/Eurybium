package github.businessdirt.eurybium.data

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.events.HandleEvent.Companion.HIGHEST
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.legacyString
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.minecraft.*
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam

@EurybiumModule
object ScoreboardData {

    var sidebarLinesFormatted: List<String> = emptyList()
        private set

    var sidebarLinesRaw: List<String> = emptyList()
        private set

    var objectiveTitle: String = ""
        private set

    private var objectiveName = ""

    @HandleEvent(events = [ TickEvent::class ], priority = HIGHEST)
    private fun onTickEvent() { refresh() }

    @HandleEvent(events = [ WorldChangeEvent::class, ClientDisconnectEvent::class ], priority = HIGHEST)
    private fun onTabListClearEvents() = clear()

    @HandleEvent
    private fun onCommandRegistrationEvent(event: CommandRegistrationEvent) = event.register("eybdumpscoreboard") {
        category = CommandCategory.DEVELOPER_DEBUG
        description = "Dump scoreboard contents to logs."
        simpleCallback {
            sidebarLinesFormatted.forEach { EurybiumMod.logger.info(it) }
            ChatAPI.debug("Dumped Scoreboard to logs")
        }
    }

    fun refresh() {
        val scoreboard = Minecraft.getInstance().level?.scoreboard
        val objective = scoreboard?.getDisplayObjective(DisplaySlot.SIDEBAR)
        val title = objective?.displayName?.string.orEmpty()
        val name = objective?.name.orEmpty()

        if (title != objectiveTitle || name != objectiveName) {
            objectiveTitle = title
            objectiveName = name
            ScoreboardTitleUpdateEvent(name, title).post()
        }

        val lines = if (objective == null) emptyList() else scoreboard.listPlayerScores(objective)
            .filterNot { it.isHidden }
            .sortedWith(compareByDescending<PlayerScoreEntry> { it.value() }
                .thenBy { it.owner().lowercase() })
            .take(15)
            .map { entry -> PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(entry.owner()), entry.ownerName()).legacyString() }

        sidebarLinesRaw = lines
        val plain = lines.map { it.removeColor() }
        if (plain != sidebarLinesFormatted) {
            val previous = sidebarLinesFormatted
            sidebarLinesFormatted = plain
            ScoreboardUpdateEvent(plain, previous).post()
        }
    }

    private fun clear() {
        val previous = sidebarLinesFormatted
        objectiveTitle = ""
        objectiveName = ""
        sidebarLinesFormatted = emptyList()
        sidebarLinesRaw = emptyList()

        ScoreboardTitleUpdateEvent("", "").post()
        if (previous.isNotEmpty()) ScoreboardUpdateEvent(emptyList(), previous).post()
    }
}
