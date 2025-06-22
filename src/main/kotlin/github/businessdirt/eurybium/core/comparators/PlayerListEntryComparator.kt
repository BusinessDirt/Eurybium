package github.businessdirt.eurybium.core.comparators

import com.google.common.collect.ComparisonChain
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.world.GameMode
import java.lang.Boolean
import kotlin.Any
import kotlin.Comparator
import kotlin.Int

class PlayerListEntryComparator : Comparator<PlayerListEntry> {
    override fun compare(o1: PlayerListEntry, o2: PlayerListEntry): Int {
        val team1 = o1.scoreboardTeam
        val team2 = o2.scoreboardTeam
        return ComparisonChain.start().compareTrueFirst(
            o1.gameMode != GameMode.SPECTATOR,
            o2.gameMode != GameMode.SPECTATOR
        ).compare(
            if (team1 != null) team1.name else "",
            if (team2 != null) team2.name else ""
        ).compare(o1.profile.name, o2.profile.name).result()
    }
}
