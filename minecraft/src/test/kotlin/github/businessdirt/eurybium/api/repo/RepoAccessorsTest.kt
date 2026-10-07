package github.businessdirt.eurybium.api.repo

import github.businessdirt.eurybium.data.repo.*
import net.minecraft.core.BlockPos
import kotlin.test.*

class RepoAccessorsTest {
    @Test
    fun `pattern handles follow snapshots and use optional fallback for missing keys`() {
        val handle = RepoPattern("example", "Fallback")
        fun snapshot(source: String) = RepoSnapshot(REVISION_A, 1, mapOf("example" to RepoPatternData("example", source, Regex(source))), emptyMap(), emptyMap())
        assertTrue(assertNotNull(handle.resolve(snapshot("First"))).matches("First"))
        assertTrue(assertNotNull(handle.resolve(snapshot("Second"))).matches("Second"))
        assertTrue(assertNotNull(handle.resolve(RepoSnapshot.EMPTY)).matches("Fallback"))
        assertNull(RepoPattern("missing").resolve(RepoSnapshot.EMPTY))
    }

    @Test
    fun `route conversion copies independent editable waypoints`() {
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to JASPER_ROUTE))
        val handle = RepoWaypointRoute("eurybium:JASP1")
        val first = assertNotNull(handle.waypoints(snapshot))
        val second = assertNotNull(handle.waypoints(snapshot))
        first[0].number = 99
        first[0].options["custom"] = "edit"
        first.removeAt(1)
        assertEquals(2, second.size)
        assertEquals(1, second[0].number)
        assertTrue(second[0].options.isEmpty())
        assertEquals(2, snapshot.routes.getValue(handle.id).points.size)
        assertNull(RepoWaypointRoute("missing").waypoints(snapshot))
    }

    @Test
    fun `template conversion requires an explicit placement`() {
        val template = JASPER_ROUTE.replace("WORLD", "TEMPLATE").replace("\"space\":", "\"layout\":\"layout-one\",\"space\":")
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to template))
        val handle = RepoWaypointRoute("eurybium:JASP1")
        assertNull(handle.waypoints(snapshot))
        val points = assertNotNull(handle.waypoints(snapshot) { BlockPos(it.x + 100, it.y, it.z + 200) })
        assertEquals(BlockPos(110, 100, 220), points[0].location)
    }

    @Test
    fun `route suggestion matching accepts lowercase prefixes for uppercase IDs`() {
        val provider = github.businessdirt.eurybium.api.commands.SuggestionProviders.dynamic { listOf("eurybium:JASP1") }
        val builder = com.mojang.brigadier.suggestion.SuggestionsBuilder("eurybium:j", 0)
        val context = com.mojang.brigadier.context.CommandContextBuilder(
            com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>(),
            github.businessdirt.eurybium.api.commands.commandSource(),
            com.mojang.brigadier.tree.RootCommandNode(),
            0,
        ).build("eurybium:j")
        assertEquals(listOf("eurybium:JASP1"), provider.getSuggestions(context, builder).join().list.map { it.text })
    }

}
