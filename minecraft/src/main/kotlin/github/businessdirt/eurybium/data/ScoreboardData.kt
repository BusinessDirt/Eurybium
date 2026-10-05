package github.businessdirt.eurybium.data

import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.events.HandleEvent.Companion.HIGHEST
import github.businessdirt.eurybium.core.utils.StringUtils.removeColor
import github.businessdirt.eurybium.core.utils.ComponentUtils.legacyString
import github.businessdirt.eurybium.events.ScoreboardUpdateEvent
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

    @HandleEvent(eventType = TickEvent::class, priority = HIGHEST)
    private fun onTickEvent() { refresh() }

    @HandleEvent(eventTypes = [ WorldChangeEvent::class, ClientDisconnectEvent::class ], priority = HIGHEST)
    private fun onTabListClearEvents() = clear()

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
