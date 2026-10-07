package github.businessdirt.eurybium.data.repo

import com.google.gson.JsonParser
import java.net.URI

/** Blocking IO service. The lifecycle API owns scheduling and publishes its results on the client thread. */
internal class RepoClient(
    private val transport: RepoTransport,
    private val cache: RepoCache,
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val onCacheFailure: (Exception) -> Unit = {},
) {

    private var cached: CachedRepo? = null

    /** Revalidates cached files before exposing them; malformed caches must not become live data. */
    @Synchronized
    fun loadCache(): RepoSnapshot? {
        val candidate = cache.read() ?: return null
        val snapshot = candidate.snapshot()
        cached = candidate

        return snapshot
    }

    /** A failed download or validation leaves both the current snapshot and cache untouched. */
    @Synchronized
    fun refresh(): RepoSnapshot? {
        val head = transport.get(URI(COMMIT_URL), cached?.etag)
        if (head.status == 304) {
            check(cached != null) { "GitHub returned Not Modified without a cache" }
            return null
        }

        check(head.status == 200) { "Repository revision request failed: HTTP ${head.status}" }

        val revision = JsonParser.parseString(head.body).asJsonObject.get("sha").asString
        require(RepoParser.validRevision(revision)) { "Invalid GitHub commit" }

        val previous = cached
        if (previous != null && revision == previous.revision) {
            // Branch metadata can get a new ETag without changing the underlying commit.
            if (head.etag != previous.etag) {
                val refreshed = previous.copy(etag = head.etag)
                persist(refreshed)
                cached = refreshed
            }

            return null
        }

        val files = linkedMapOf<String, String>()
        for (path in RepoParser.requiredFiles + RepoParser.optionalFiles) {
            val response = transport.get(URI("$RAW_URL/$revision/$path"), null)
            when (response.status) {
                200 -> files[path] = response.body
                404 if path in RepoParser.optionalFiles -> Unit
                else -> error("Repository file $path failed: HTTP ${response.status}")
            }
        }

        // Count bytes once per response, rather than repeatedly encoding all prior survey files.
        var totalBytes = files.values.sumOf { it.toByteArray(Charsets.UTF_8).size.toLong() }
        require(totalBytes <= RepoParser.MAX_TOTAL_FILE_BYTES) { "Repository data is too large" }

        // Referenced files are mandatory: a partial survey must never replace the last good revision.
        for (path in files["mining/nodes.json"]?.let(RepoParser::nodeFiles).orEmpty()) {
            val response = transport.get(URI("$RAW_URL/$revision/$path"), null)
            check(response.status == 200) { "Repository file $path failed: HTTP ${response.status}" }
            files[path] = response.body
            totalBytes += response.body.toByteArray(Charsets.UTF_8).size
            require(totalBytes <= RepoParser.MAX_TOTAL_FILE_BYTES) {
                "Repository data is too large"
            }
        }

        val candidate = CachedRepo(revision, head.etag, nowMillis(), files)
        val snapshot = candidate.snapshot()

        persist(candidate)
        cached = candidate

        return snapshot
    }

    private fun persist(candidate: CachedRepo) = try {
        cache.save(candidate)
    } catch (failure: Exception) {
        // Fresh validated data remains usable when persistence fails; keep the old disk cache.
        onCacheFailure(failure)
    }

    companion object {
        const val COMMIT_URL = "https://api.github.com/repos/BusinessDirt/Eurybium-Data/commits/master"
        private const val RAW_URL = "https://raw.githubusercontent.com/BusinessDirt/Eurybium-Data"
    }
}
