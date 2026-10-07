package github.businessdirt.eurybium.data.repo

import github.businessdirt.eurybium.data.model.MineshaftType
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URI
import kotlin.test.*

private const val NODE_PATH = "mining/nodes/JASP1.json"
private const val NODE_INDEX = """{"schemaVersion":1,"files":["mining/nodes/JASP1.json"]}"""
private const val NODE_FILE = """{"schemaVersion":1,"island":"MINESHAFT","mineshaft":"JASP1","space":"WORLD","nodes":[{"id":"JASP1/magenta_glass/10_100_20","kind":"GEMSTONE","material":"MAGENTA_GLASS","blockTypes":["minecraft:magenta_stained_glass","minecraft:magenta_stained_glass_pane"],"blocks":[[10,100,20],[11,100,20],[10,100,20]]}]}"""

class RepoNodeFilesTest {
    @TempDir lateinit var directory: File

    private fun files() = repoFiles() + mapOf("mining/nodes.json" to NODE_INDEX, NODE_PATH to NODE_FILE)

    @Test
    fun `node files resolve typed mineshafts and retain immutable deduplicated blocks`() {
        val node = RepoParser.parse(REVISION_A, 1, files()).nodes.values.single()
        assertEquals(MineshaftType.JASP_1, node.mineshaft)
        assertEquals(NODE_PATH, node.sourceFile)
        assertEquals(2, node.blocks.size)
        assertEquals(listOf("minecraft:magenta_stained_glass", "minecraft:magenta_stained_glass_pane"), node.blockTypes)
        assertFailsWith<UnsupportedOperationException> { (node.blockTypes as MutableList).clear() }
        assertFailsWith<UnsupportedOperationException> { (node.blocks as MutableList).clear() }
    }

    @Test
    fun `existing Python exported fixture remains compatible`() {
        val generated = javaClass.getResource("/repo/world-nodes.json")!!.readText()
        val node = RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to generated)).nodes.values.single()
        assertEquals("JASP1/red_glass/-1_-16_0", node.id)
        assertEquals(listOf(RepoPosition(-1, -16, 0), RepoPosition(0, -16, 0)), node.blocks)
        assertEquals("RED_GLASS", node.material)
    }

    @Test
    fun `index accepts one file per known mineshaft and rejects unsafe duplicate or split paths`() {
        for (path in listOf("../other.json", "https://host/nodes.json", "mining/nodes/a/b.json",
            "mining/nodes/UNKNOWN.json", "mining/nodes/JASP1-part-001.json", "mining/nodes/crystal-hollows.json")) {
            assertFails { RepoParser.nodeFiles(NODE_INDEX.replace(NODE_PATH, path)) }
        }
        assertFails { RepoParser.nodeFiles("""{"schemaVersion":1,"files":["$NODE_PATH","$NODE_PATH"]}""") }
        val paths = (0..RepoParser.MAX_NODE_FILES).joinToString { "\"$NODE_PATH\"" }
        assertFails { RepoParser.nodeFiles("""{"schemaVersion":1,"files":[$paths]}""") }
        assertFails { RepoParser.nodeFiles("""{"schemaVersion":1,"nodes":[]}""") }
        assertTrue(RepoParser.nodeFiles("""{"schemaVersion":1,"files":[]}""").isEmpty())
    }

    @Test
    fun `unsupported scope kind block types and missing data reject candidate`() {
        assertFails { RepoParser.parse(REVISION_A, 1, files() - NODE_PATH) }
        for (text in listOf("{bad}", NODE_FILE.replace("\"mineshaft\":\"JASP1\"", "\"mineshaft\":\"JASPC\""),
            NODE_FILE.replace("MINESHAFT", "CRYSTAL_HOLLOWS"), NODE_FILE.replace("WORLD", "TEMPLATE"),
            NODE_FILE.replace("GEMSTONE", "ORE"), NODE_FILE.replace("GEMSTONE", "MITHRIL"),
            NODE_FILE.replace("\"kind\":", "\"island\":\"HUB\",\"kind\":"),
            NODE_FILE.replace("minecraft:magenta_stained_glass", "minecraft:iron_ore"),
            NODE_FILE.replace("\"blockTypes\":[\"minecraft:magenta_stained_glass\",\"minecraft:magenta_stained_glass_pane\"]", "\"blockTypes\":[]"))) {
            assertFails { RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to text)) }
        }
    }

    @Test
    fun `IDs remain globally unique and unlisted files cannot enter a snapshot`() {
        val secondPath = "mining/nodes/JASPC.json"
        val index = """{"schemaVersion":1,"files":["$NODE_PATH","$secondPath"]}"""
        val second = NODE_FILE.replace("\"mineshaft\":\"JASP1\"", "\"mineshaft\":\"JASPC\"")
        val combined = files() + mapOf("mining/nodes.json" to index, secondPath to second)
        assertFails { RepoParser.parse(REVISION_A, 1, combined) }
        val unique = second.replace("JASP1/magenta_glass", "JASPC/magenta_glass")
        assertEquals(2, RepoParser.parse(REVISION_A, 1, combined + (secondPath to unique)).nodes.size)
        assertFails { RepoParser.parse(REVISION_A, 1, files() + (secondPath to unique)) }
    }

    @Test
    fun `empty mineshaft surveys are valid`() {
        val empty = """{"schemaVersion":1,"island":"MINESHAFT","mineshaft":"JASP1","space":"WORLD","nodes":[]}"""
        assertTrue(RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to empty)).nodes.isEmpty())
    }

    @Test
    fun `client downloads pinned shaft files caches them and preserves old data on missing file`() {
        var revision = REVISION_A
        var missing = false
        val requests = mutableListOf<URI>()
        val transport = RepoTransport { uri, _ ->
            requests += uri
            if (uri.toString() == RepoClient.COMMIT_URL) RepoResponse(200, """{"sha":"$revision"}""")
            else {
                val path = uri.path.substringAfter("/$revision/")
                if (missing && path == NODE_PATH) RepoResponse(404)
                else files()[path]?.let { RepoResponse(200, it) } ?: RepoResponse(404)
            }
        }
        val cache = RepoCache(File(directory, "cache.json"))
        val client = RepoClient(transport, cache)
        assertEquals(1, assertNotNull(client.refresh()).nodes.size)
        assertTrue(requests.drop(1).all { "/$REVISION_A/" in it.path })
        val offline = RepoClient(RepoTransport { _, _ -> error("Offline") }, cache).loadCache()
        assertEquals(MineshaftType.JASP_1, assertNotNull(offline).nodes.values.single().mineshaft)
        missing = true
        revision = REVISION_B
        assertFails { client.refresh() }
        assertEquals(REVISION_A, cache.read()?.revision)
    }
}
