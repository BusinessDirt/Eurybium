package github.businessdirt.eurybium.data.repo

import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URI
import kotlin.test.*

private const val NODE_PATH = "mining/nodes/JASP1.json"
private const val NODE_INDEX = """{"schemaVersion":1,"files":["mining/nodes/JASP1.json"]}"""
private const val NODE_FILE = """{"schemaVersion":1,"island":"MINESHAFT","mineshaft":"JASP1","space":"WORLD","nodes":[{"id":"JASP1/magenta_glass/10_100_20","kind":"GEMSTONE","material":"MAGENTA_GLASS","blockTypes":["minecraft:magenta_stained_glass","minecraft:magenta_stained_glass_pane"],"blocks":[[10,100,20],[11,100,20]]}]}"""

class RepoNodeFilesTest {
    @TempDir lateinit var directory: File

    private fun files() = repoFiles() + mapOf("mining/nodes.json" to NODE_INDEX, NODE_PATH to NODE_FILE)

    @Test
    fun `node file shares validated scope and retains immutable block IDs`() {
        val node = RepoParser.parse(REVISION_A, 1, files()).nodes.values.single()
        assertEquals("JASP1", node.scope.mineshaft)
        assertEquals(2, node.blocks.size)
        assertEquals(listOf("minecraft:magenta_stained_glass", "minecraft:magenta_stained_glass_pane"), node.blockTypes)
        assertFailsWith<UnsupportedOperationException> { (node.blockTypes as MutableList).clear() }
    }

    @Test
    fun `Python exported fixture is accepted without translation`() {
        val generated = javaClass.getResource("/repo/world-nodes.json")!!.readText()
        val node = RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to generated)).nodes.values.single()
        assertEquals("JASP1/red_glass/-1_-16_0", node.id)
        assertEquals(listOf(RepoPosition(-1, -16, 0), RepoPosition(0, -16, 0)), node.blocks)
        assertEquals("RED_GLASS", node.material)
    }

    @Test
    fun `index rejects unsafe duplicate and excessive file paths`() {
        for (path in listOf("../other.json", "https://host/nodes.json", "mining/nodes/a/b.json", "mining/nodes/a%20b.json")) {
            assertFails { RepoParser.nodeFiles(NODE_INDEX.replace(NODE_PATH, path)) }
        }
        assertFails { RepoParser.nodeFiles("""{"schemaVersion":1,"files":["$NODE_PATH","$NODE_PATH"]}""") }
        val paths = (0..RepoParser.MAX_NODE_FILES).joinToString { "\"mining/nodes/$it.json\"" }
        assertFails { RepoParser.nodeFiles("""{"schemaVersion":1,"files":[$paths]}""") }
    }

    @Test
    fun `missing malformed conflicting scope and invalid block IDs reject candidate`() {
        assertFails { RepoParser.parse(REVISION_A, 1, files() - NODE_PATH) }
        for (text in listOf("{bad}", NODE_FILE.replace("JASP1", "UNKNOWN"),
            NODE_FILE.replace("\"kind\":", "\"island\":\"HUB\",\"kind\":"),
            NODE_FILE.replace("minecraft:magenta_stained_glass", "Not a block"),
            NODE_FILE.replace("\"blockTypes\":[\"minecraft:magenta_stained_glass\",\"minecraft:magenta_stained_glass_pane\"]", "\"blockTypes\":[]"))) {
            assertFails { RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to text)) }
        }
    }

    @Test
    fun `legacy inline nodes and shards coexist but IDs remain globally unique`() {
        val index = NODE_INDEX.dropLast(1) + ",\"nodes\":" + JASPER_NODE.substringAfter("\"nodes\":").dropLast(1) + "}"
        val mixed = files() + ("mining/nodes.json" to index)
        assertEquals(2, RepoParser.parse(REVISION_A, 1, mixed).nodes.size)
        val duplicate = NODE_FILE.replace("JASP1/magenta_glass/10_100_20", "jasper-one")
        assertFails { RepoParser.parse(REVISION_A, 1, mixed + (NODE_PATH to duplicate)) }
        assertFails { RepoParser.parse(REVISION_A, 1, files() + ("mining/nodes/unlisted.json" to NODE_FILE)) }
    }

    @Test
    fun `empty surveys and template surveys are explicit`() {
        val empty = """{"schemaVersion":1,"island":"CRYSTAL_HOLLOWS","space":"WORLD","nodes":[]}"""
        assertTrue(RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to empty)).nodes.isEmpty())
        val template = NODE_FILE.replace("\"WORLD\"", "\"TEMPLATE\"")
        assertFails { RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to template)) }
        val valid = template.replace("\"nodes\":", "\"layout\":\"jasper-layout\",\"nodes\":")
        assertEquals(RepoCoordinateSpace.TEMPLATE, RepoParser.parse(REVISION_A, 1, files() + (NODE_PATH to valid)).nodes.values.single().scope.space)
    }

    @Test
    fun `client downloads pinned node files caches them and retains old data on missing shard`() {
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
        assertEquals(2, assertNotNull(offline).nodes.values.single().blockTypes.size)
        missing = true
        revision = REVISION_B
        assertFails { client.refresh() }
        assertEquals(REVISION_A, cache.read()?.revision)
    }
}
