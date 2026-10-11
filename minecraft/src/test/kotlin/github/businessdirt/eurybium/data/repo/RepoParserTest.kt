package github.businessdirt.eurybium.data.repo

import kotlin.test.*

class RepoParserTest {
    @Test
    fun `current empty catalogs work without future mining files`() {
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles())
        assertTrue(snapshot.patterns.isEmpty())
        assertTrue(snapshot.routes.isEmpty())
    }

    @Test
    fun `patterns are compiled once and IDs are global across files`() {
        val text = """{"schemaVersion":2,"patterns":{"ability.ready":"Ready (?<ability>.+)"}}"""
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text))
        assertEquals("Pickaxe", snapshot.patterns.getValue("ability.ready").regex.matchEntire("Ready Pickaxe")?.groups?.get("ability")?.value)
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + mapOf("patterns/chat.json" to text, "patterns/scoreboard.json" to text)) }
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text.replace("Ready (?<ability>.+)", "["))) }
    }

    @Test
    fun `version 2 preserves escaped regexes unicode symbols and named groups`() {
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/scoreboard.json" to SCOREBOARD_PATTERNS))
        assertEquals(setOf("skyblock.area.plain", "hypixel.lobby.type"), snapshot.patterns.keys)
        val area = snapshot.patterns.getValue("skyblock.area.plain")
        assertEquals("skyblock.area.plain", area.id)
        assertEquals("""\s*(?<symbol>[⏣ф])\s+(?<area>.+)""", area.source)
        for (symbol in listOf("⏣", "ф", "")) {
            val match = assertNotNull(area.regex.matchEntire("  $symbol Dwarven Mines"))
            assertEquals(symbol, match.groups["symbol"]?.value)
            assertEquals("Dwarven Mines", match.groups["area"]?.value)
        }
        assertEquals("meg lobby", snapshot.patterns.getValue("hypixel.lobby.type").regex.matchEntire("meg lobby12")?.groups?.get("type")?.value)
    }

    @Test
    fun `version 1 patterns remain readable alongside version 2 catalogs`() {
        val legacy = """{"schemaVersion":1,"patterns":[{"id":"ability.ready","pattern":"Ready (?<ability>.+)"}]}"""
        val files = repoFiles() + mapOf("patterns/chat.json" to legacy, "patterns/scoreboard.json" to SCOREBOARD_PATTERNS)
        val snapshot = RepoParser.parse(REVISION_A, 1, files)
        assertEquals(3, snapshot.patterns.size)
        assertTrue(snapshot.patterns.getValue("ability.ready").regex.matches("Ready Pickaxe"))
        val duplicate = """{"schemaVersion":2,"patterns":{"ability.ready":"Ready"}}"""
        assertFails { RepoParser.parse(REVISION_A, 1, files + ("patterns/tab.json" to duplicate)) }
    }

    @Test
    fun `version 2 requires an object with valid IDs and string regexes`() {
        val invalidPatterns = listOf("[]", "null", "42", "true", "\"pattern\"", """{"":"valid"}""", """{"bad id":"valid"}""") +
            listOf("null", "42", "true", "[]", "{}").map { """{"valid.id":$it}""" } +
            listOf("""{"${"a".repeat(129)}":"valid"}""", """{"valid.id":"${"a".repeat(4097)}"}""")
        for (patterns in invalidPatterns) {
            val text = """{"schemaVersion":2,"patterns":$patterns}"""
            assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text)) }
        }
        for (text in listOf("""{"schemaVersion":2}""", """{"schemaVersion":1,"patterns":{}}""")) {
            assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text)) }
        }
        val tooMany = (1..20_001).joinToString(",") { "\"pattern.$it\":\"valid\"" }
        assertFails {
            RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to """{"schemaVersion":2,"patterns":{$tooMany}}"""))
        }
    }

    @Test
    fun `invalid revisions schemas and missing required files are rejected`() {
        assertFails { RepoParser.parse("master", 1, repoFiles()) }
        assertFails { RepoParser.parse(REVISION_A, -1, repoFiles()) }
        assertFails { RepoParser.parse(REVISION_A, 1, emptyMap()) }
        for (text in listOf(EMPTY_PATTERNS.replace(":2", ":3"), "[]", "{bad}")) {
            assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("patterns/chat.json" to text)) }
        }
        assertFails { RepoParser.parse(REVISION_A, 1, repoFiles() + ("other.json" to "{}")) }
    }

    @Test
    fun `routes have validated scopes and ordered points`() {
        val snapshot = RepoParser.parse(REVISION_A, 1, repoFiles() + ("mining/routes.json" to JASPER_ROUTE))
        assertEquals(2, snapshot.routes.getValue("eurybium:JASP1").points.size)
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
        val snapshot = RepoSnapshot(REVISION_A, 1, emptyMap(), input)
        input.clear()
        points.clear()
        assertEquals(1, snapshot.routes.size)
        assertEquals(1, route.points.size)
        assertFailsWith<UnsupportedOperationException> { (snapshot.routes as MutableMap).clear() }
        assertFailsWith<UnsupportedOperationException> { (route.points as MutableList).clear() }
    }
}
