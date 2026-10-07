package github.businessdirt.eurybium.data.repo

import kotlin.test.*

class RepoParserTest {
    @Test
    fun `current empty catalogs work without future mining files`() {
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles())
        assertTrue(snapshot.patterns.isEmpty())
        assertTrue(snapshot.routes.isEmpty())
        assertTrue(snapshot.nodes.isEmpty())
    }

    @Test
    fun `patterns are compiled once and IDs are global across files`() {
        val text = """{"schemaVersion":1,"patterns":[{"id":"ability.ready","pattern":"Ready (?<ability>.+)"}]}"""
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text))
        assertEquals("Pickaxe", snapshot.patterns.getValue("ability.ready").regex.matchEntire("Ready Pickaxe")?.groups?.get("ability")?.value)
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + mapOf("patterns/chat.json" to text, "patterns/scoreboard.json" to text)) }
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text.replace("Ready (?<ability>.+)", "["))) }
    }

    @Test
    fun `invalid revisions schemas and missing required files are rejected`() {
        assertFails { RepoParser.parse("master", 1, repoFiles()) }
        assertFails { RepoParser.parse(REVISION_A, -1, repoFiles()) }
        assertFails { RepoParser.parse(REVISION_A, 1, emptyMap()) }
        for (text in listOf(EMPTY_PATTERNS.replace(":1", ":2"), "[]", "{bad}")) {
            assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text)) }
        }
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("other.json" to "{}")) }
    }

    @Test
    fun `routes and nodes have validated scopes and deduplicated block membership`() {
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles() + mapOf("mining/routes.json" to JASPER_ROUTE, "mining/nodes.json" to JASPER_NODE))
        assertEquals(2, snapshot.routes.getValue("eurybium:JASP1").points.size)
        assertEquals(2, snapshot.nodes.getValue("jasper-one").blocks.size)
        for (text in listOf(JASPER_ROUTE.replace("JASP1", "JASP2"), JASPER_ROUTE.replace("MINESHAFT", "HUB"), JASPER_ROUTE.replace("[10,100,20]", "[10.5,100,20]"), JASPER_ROUTE.replace("[10,100,20]", "[10,100]"))) {
            assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to text)) }
        }
    }

    @Test
    fun `all current spawning routes are accepted only in their spawning region`() {
        for (suffix in listOf("MITHRIL", "TUNGSTEN", "GEMSTONES")) {
            val text = """{"schemaVersion":1,"routes":[{"id":"eurybium:SHAFT_SPAWN_$suffix","island":"DWARVEN_MINES","region":"DWARVEN_BASE_CAMP","space":"WORLD","points":[[0,100,0]]}]}"""
            assertEquals(1, RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to text)).routes.size)
            assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to text.replace("DWARVEN_BASE_CAMP", "OTHER"))) }
        }
    }

    @Test
    fun `template records require a layout and oversized files are rejected`() {
        val template = JASPER_ROUTE.replace("WORLD", "TEMPLATE")
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to template)) }
        val valid = template.replace("\"space\":", "\"layout\":\"jasper-layout\",\"space\":")
        assertEquals(RepoCoordinateSpace.TEMPLATE, RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to valid)).routes.values.single().scope.space)
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to " ".repeat(RepoParser.MAX_FILE_BYTES + 1))) }
    }

    @Test
    fun `snapshot containers cannot be changed by consumers or source container edits`() {
        val input = linkedMapOf<String, RepoRoute>()
        val points = mutableListOf(RepoPosition(1, 2, 3))
        val route = RepoRoute("example", RepoScope(github.businessdirt.eurybium.data.model.IslandType.HUB, null, null, RepoCoordinateSpace.WORLD, null), points)
        input[route.id] = route
        val snapshot = RepoSnapshot(REVISION_A, 1, emptyMap(), input, emptyMap())
        input.clear()
        points.clear()
        assertEquals(1, snapshot.routes.size)
        assertEquals(1, route.points.size)
        assertFailsWith<UnsupportedOperationException> { (snapshot.routes as MutableMap).clear() }
        assertFailsWith<UnsupportedOperationException> { (route.points as MutableList).clear() }
    }
}
