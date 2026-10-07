package github.businessdirt.eurybium.data.repo

import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URI
import kotlin.test.*

class RepoClientTest {
    @TempDir lateinit var directory: File

    private class Transport : RepoTransport {
        val requests = mutableListOf<Pair<URI, String?>>()
        var revision = REVISION_A
        var status = 200
        var responseEtag: String? = null
        var files = repoFiles()
        override fun get(uri: URI, etag: String?): RepoResponse {
            requests += uri to etag
            if (uri.toString() == RepoClient.COMMIT_URL) return RepoResponse(status, """{"sha":"$revision"}""", responseEtag ?: "etag-$revision")
            val path = uri.path.substringAfter("/$revision/")
            return files[path]?.let { RepoResponse(200, it) } ?: RepoResponse(404)
        }
    }

    private fun cache() = RepoCache(File(directory, "repo/cache.json"))

    @Test
    fun `fresh update uses one commit for every file and writes a reusable offline cache`() {
        val transport = Transport().apply { files = repoFiles() + ("mining/routes.json" to JASPER_ROUTE) }
        val client = RepoClient(transport, cache(), { 123 })
        assertNull(client.loadCache())
        val updated = assertNotNull(client.refresh())
        assertEquals(123, updated.fetchedAtMillis)
        assertEquals(1, updated.routes.size)
        assertTrue(transport.requests.drop(1).all { "/$REVISION_A/" in it.first.path })
        assertEquals(5, transport.requests.size)
        val offline = RepoClient(RepoTransport { _, _ -> error("Must not fetch while restoring cache") }, cache()).loadCache()
        assertEquals(REVISION_A, assertNotNull(offline).revision)
        assertEquals(updated.routes.keys, offline.routes.keys)
    }

    @Test
    fun `not modified and unchanged commit do not download catalogs`() {
        val transport = Transport()
        val client = RepoClient(transport, cache())
        client.refresh()
        transport.requests.clear()
        transport.status = 304
        assertNull(client.refresh())
        assertEquals(1, transport.requests.size)
        assertEquals("etag-$REVISION_A", transport.requests.single().second)
        transport.status = 200
        transport.requests.clear()
        assertNull(client.refresh())
        assertEquals(1, transport.requests.size)
    }

    @Test
    fun `same commit can refresh its ETag without downloading or publishing another snapshot`() {
        val transport = Transport()
        val client = RepoClient(transport, cache())
        client.refresh()
        transport.requests.clear()
        transport.responseEtag = "new-etag"
        assertNull(client.refresh())
        assertEquals(1, transport.requests.size)
        assertEquals("new-etag", cache().read()?.etag)
        transport.status = 304
        transport.requests.clear()
        assertNull(client.refresh())
        assertEquals("new-etag", transport.requests.single().second)
    }

    @Test
    fun `invalid new data leaves old disk cache and revision intact`() {
        val transport = Transport()
        val client = RepoClient(transport, cache())
        client.refresh()
        transport.revision = REVISION_B
        transport.files = repoFiles() + ("mining/routes.json" to "{bad}")
        assertFails { client.refresh() }
        assertEquals(REVISION_A, cache().read()?.revision)
        transport.status = 304
        transport.requests.clear()
        assertNull(client.refresh())
        assertEquals("etag-$REVISION_A", transport.requests.single().second)
    }

    @Test
    fun `missing required file and unsuccessful commit request do not replace the cache`() {
        val transport = Transport()
        val client = RepoClient(transport, cache())
        client.refresh()
        transport.revision = REVISION_B
        transport.files = emptyMap()
        assertFails { client.refresh() }
        assertEquals(REVISION_A, cache().read()?.revision)
        for (status in listOf(403, 429, 500)) {
            transport.status = status
            assertFails { client.refresh() }
            assertEquals(REVISION_A, cache().read()?.revision)
        }
    }

    @Test
    fun `invalid cache cannot enter memory and a fresh download can recover`() {
        val file = File(directory, "repo/cache.json")
        file.parentFile.mkdirs()
        file.writeText("{bad}")
        val client = RepoClient(Transport(), cache())
        assertFails { client.loadCache() }
        assertEquals(REVISION_A, assertNotNull(client.refresh()).revision)
        assertEquals(REVISION_A, cache().read()?.snapshot()?.revision)
    }

    @Test
    fun `cache write failure still returns validated data and reports persistence failure`() {
        val blocker = File(directory, "blocked").apply { writeText("file") }
        val failures = mutableListOf<Exception>()
        val client = RepoClient(Transport(), RepoCache(File(blocker, "cache.json")), onCacheFailure = failures::add)
        assertEquals(REVISION_A, assertNotNull(client.refresh()).revision)
        assertEquals(1, failures.size)
    }

    @Test
    fun `not modified without a validated cache is rejected`() {
        val client = RepoClient(Transport().apply { status = 304 }, cache())
        assertFails { client.refresh() }
    }
}
