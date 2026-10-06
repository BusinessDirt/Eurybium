package github.businessdirt.eurybium.api.hypixelapi

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.hypixel.HypixelApiServerChangeEvent
import github.businessdirt.eurybium.events.hypixel.HypixelLeaveEvent
import github.businessdirt.eurybium.events.minecraft.ScoreboardTitleUpdateEvent
import github.businessdirt.eurybium.events.skyblock.IslandJoinEvent
import github.businessdirt.eurybium.events.skyblock.IslandLeaveEvent
import net.hypixel.data.type.GameType
import kotlin.test.*

class HypixelLocationTrackerTest {
    private val events = mutableListOf<EurybiumEvent>()
    private val tracker = HypixelLocationTracker(events::add)
    private fun change(mode: String?, server: String = "mini1") = HypixelApiServerChangeEvent(server, GameType.SKYBLOCK, null, mode, null)

    @Test
    fun `transitions publish snapshots before emitting captured leave and join payloads`() {
        tracker.joined(true)
        tracker.serverChanged(change("hub"))
        val snapshot = tracker.state
        tracker.serverChanged(change("combat_3", "mini2"))
        assertTrue(snapshot.inAlpha)
        assertEquals(IslandType.HUB, snapshot.island)
        assertEquals(IslandType.THE_END, tracker.state.island)
        assertEquals(listOf(IslandJoinEvent::class, IslandLeaveEvent::class, IslandJoinEvent::class), events.map { it::class })
        assertEquals(IslandType.HUB, (events[1] as IslandLeaveEvent).island)
        val joined = events[2] as IslandJoinEvent
        assertEquals(IslandType.THE_END, joined.island)
        assertEquals(IslandType.HUB, joined.previousIsland)
    }

    @Test
    fun `guest island waits for the correct scoreboard and duplicates cannot undo confirmation`() {
        tracker.serverChanged(change("hub"))
        tracker.serverChanged(change("garden", "mini2"))
        assertEquals(IslandType.NONE, tracker.state.island)
        assertEquals(2, events.size)
        tracker.scoreboardUpdated(ScoreboardTitleUpdateEvent("other", "SKYBLOCK GUEST"))
        assertEquals(IslandType.NONE, tracker.state.island)
        tracker.scoreboardUpdated(ScoreboardTitleUpdateEvent("SBScoreboard", "§aSKYBLOCK GUEST  "))
        assertTrue(tracker.state.isGuest)
        assertEquals(IslandType.GARDEN_GUEST, tracker.state.island)
        assertEquals(IslandType.HUB, (events.last() as IslandJoinEvent).previousIsland)
        tracker.serverChanged(change("garden", "mini2"))
        tracker.scoreboardUpdated(ScoreboardTitleUpdateEvent("SBScoreboard", "SKYBLOCK"))
        assertEquals(IslandType.GARDEN_GUEST, tracker.state.island)
        assertEquals(3, events.size)
    }

    @Test
    fun `metadata updates preserve a confirmed guest island and do not emit another join`() {
        tracker.serverChanged(change("garden"))
        tracker.scoreboardUpdated(ScoreboardTitleUpdateEvent("SBScoreboard", "SKYBLOCK GUEST"))
        tracker.serverChanged(change("garden").copy(map = "updated map"))
        assertEquals("updated map", tracker.state.map)
        assertEquals(IslandType.GARDEN_GUEST, tracker.state.island)
        assertTrue(tracker.state.isGuest)
        assertEquals(1, events.size)
    }

    @Test
    fun `owner confirmation and a second guest transition use the new pending island`() {
        tracker.serverChanged(change("dynamic"))
        tracker.serverChanged(change("garden", "mini2"))
        tracker.scoreboardUpdated(ScoreboardTitleUpdateEvent("SBScoreboard", "SKYBLOCK"))
        assertEquals(IslandType.GARDEN, tracker.state.island)
        assertFalse(tracker.state.isGuest)
        assertEquals(1, events.size)
    }

    @Test
    fun `missing or unknown mode cannot keep the old island and leaving skyblock clears pending guest`() {
        tracker.serverChanged(change("hub"))
        tracker.serverChanged(change(null, "mini2"))
        assertEquals(IslandType.UNKNOWN, tracker.state.island)
        tracker.serverChanged(change("new_mode", "mini3"))
        assertEquals(IslandType.UNKNOWN, tracker.state.island)
        tracker.serverChanged(change("dynamic", "mini4"))
        tracker.serverChanged(HypixelApiServerChangeEvent("main", null, "mainlobby12", null, null))
        tracker.scoreboardUpdated(ScoreboardTitleUpdateEvent("SBScoreboard", "SKYBLOCK GUEST"))
        assertFalse(tracker.state.inSkyBlock)
        assertEquals(IslandType.NONE, tracker.state.island)
        assertEquals("mainlobby", tracker.state.lobbyType)
        assertTrue(tracker.state.inLobby)
    }

    @Test
    fun `disconnect clears all metadata and history and does not emit duplicate leaves`() {
        tracker.joined(true)
        tracker.serverChanged(change("hub"))
        tracker.disconnected()
        assertEquals(HypixelLocationState(), tracker.state)
        assertIs<IslandLeaveEvent>(events[1])
        assertIs<HypixelLeaveEvent>(events[2])
        tracker.disconnected()
        assertEquals(3, events.size)
        tracker.serverChanged(change("combat_3"))
        assertEquals(IslandType.NONE, (events.last() as IslandJoinEvent).previousIsland)
    }

    @Test
    fun `disconnect while waiting for scoreboard cannot create a late guest join`() {
        tracker.serverChanged(change("garden"))
        tracker.disconnected()
        tracker.scoreboardUpdated(ScoreboardTitleUpdateEvent("SBScoreboard", "SKYBLOCK GUEST"))
        assertEquals(HypixelLocationState(), tracker.state)
        assertEquals(listOf(HypixelLeaveEvent::class), events.map { it::class })
    }

    @Test
    fun `snapshots require connection and skyblock for island checks and parse lobby names strictly`() {
        val disconnected = HypixelLocationState(serverType = GameType.SKYBLOCK, island = IslandType.HUB)
        assertFalse(disconnected.inAnyIsland(listOf(IslandType.HUB)))
        assertTrue(disconnected.copy(inHypixel = true).inAnyIsland(listOf(IslandType.HUB)))
        assertFalse(disconnected.copy(inHypixel = true).inAnyIsland(listOf(IslandType.GARDEN)))
        assertEquals("mainlobby", HypixelLocationState(lobbyName = "mainlobby123").lobbyType)
        assertNull(HypixelLocationState(lobbyName = "mainlobby").lobbyType)
        assertFalse(HypixelLocationState(lobbyName = "  ").inLobby)
        assertFalse(HypixelLocationState(serverId = "limbo").inLimbo)
        assertTrue(HypixelLocationState(inHypixel = true, serverId = "limbo").inLimbo)
    }
}
