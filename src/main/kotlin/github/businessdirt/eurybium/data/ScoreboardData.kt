package github.businessdirt.eurybium.data

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.ScoreboardUpdateEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardTitleUpdateEvent
import github.businessdirt.eurybium.events.minecraft.packet.PacketReceivedEvent
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import github.businessdirt.eurybium.utils.StringUtils.removeColor
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.TeamS2CPacket
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import kotlin.time.Duration

object ScoreboardData {

    var sidebarLinesFormatted: List<String> = emptyList()
        private set
    var sidebarLinesRaw: List<String> = emptyList()
        private set

    private var monitor = false
    private var lastMonitorState: List<String> = emptyList()
    private var lastChangeTime: SimpleTimeMark = SimpleTimeMark.farPast()

    val objectiveTitle: String
        get() = MinecraftClient.getInstance().player?.scoreboard?.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)?.displayName?.string.orEmpty()


    @HandleEvent(receiveCancelled = true)
    fun onPacketReceiveEvent(event: PacketReceivedEvent) {
        when (val packet = event.packet) {
            is ScoreboardScoreUpdateS2CPacket -> {
                if ("update" == packet.objectiveName()) this.onScoreboardUpdated()
            }

            is TeamS2CPacket -> {
                if (packet.teamName.startsWith("team_")) this.onScoreboardUpdated()
            }

            is ScoreboardObjectiveUpdateS2CPacket -> {
                if (packet.type != ScoreboardCriterion.RenderType.INTEGER || "health" == packet.name) return
                ScoreboardTitleUpdateEvent(packet.displayName.string, packet.name).post()
            }
        }
    }

    private fun onScoreboardUpdated() {
        monitor()

        val scoreboardLines = fetchScoreboardLines().reversed()
        val semiFormatted = scoreboardLines.map { s ->
            s.toCharArray().filter {
                // 10735 = Rift Blood Effigies symbol
                it.code in 21..126 || it.code == 167 || it.code == 10735
            }.joinToString(separator = "")
        }

        if (semiFormatted != sidebarLinesRaw) {
            sidebarLinesRaw = semiFormatted
            // raw scoreboard update event
        }

        val newLines = scoreboardLines.map { it.removeColor() }
        if (newLines != sidebarLinesFormatted) {
            val old = sidebarLinesFormatted
            sidebarLinesFormatted = newLines
            ScoreboardUpdateEvent(newLines, old).post()
        }
    }

    @HandleEvent
    fun onCommandRegistrationEvent(event: CommandRegistrationEvent) {
        event.register("eurybiumdebugscoreboard") {
            description = "Monitors the scoreboard changes: Prints the raw scoreboard lines in the console after each update, with time since last update."
            category = CommandCategory.DEVELOPER_DEBUG
            simpleCallback {
                monitor = !monitor
                val action = if (monitor) "Enabled" else "Disabled"
                EurybiumMod.logger.debug("{} scoreboard monitoring in the console.", action)
            }
        }
    }

    private fun monitor() {
        if (!monitor) return
        val currentList: List<String> = fetchScoreboardLines()
        if (lastMonitorState !== currentList) {
            val time: Duration = lastChangeTime.passedSince()
            lastChangeTime = SimpleTimeMark.now()
            EurybiumMod.logger.debug("Scoreboard Monitor: (new change after {}s)", time.inWholeSeconds)
            for (line in currentList) EurybiumMod.logger.debug("'{}'", line)
        }

        lastMonitorState = currentList
        EurybiumMod.logger.emptyDebug()
    }

    private fun fetchScoreboardLines(): List<String> {
        val scoreboard = MinecraftClient.getInstance().world?.scoreboard ?: return emptyList()
        val sidebarObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()

        return scoreboard.getScoreboardEntries(sidebarObjective)
            .sortedByDescending { it.value }
            .map { entry ->
                val name = entry.owner
                val team = scoreboard.getScoreHolderTeam(name)
                if (team == null) name
                else team.color.toString() + team.prefix.string + name + team.suffix.string
            }
    }
}
