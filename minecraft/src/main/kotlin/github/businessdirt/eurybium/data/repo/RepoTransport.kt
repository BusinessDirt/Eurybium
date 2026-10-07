package github.businessdirt.eurybium.data.repo

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.TimeUnit

/** Injectable transport keeps repository update tests independent of GitHub and the Minecraft client. */
internal fun interface RepoTransport {
    fun get(uri: URI, etag: String?): RepoResponse
}

internal data class RepoResponse(val status: Int, val body: String = "", val etag: String? = null)

/** Bounded bodies and whole-response deadlines apply even when the server stalls during a download. */
internal class HttpRepoTransport : RepoTransport {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

    override fun get(uri: URI, etag: String?): RepoResponse {
        val request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", "Eurybium-Repository")
            .header("Accept", "application/json")
            .apply { if (etag != null) header("If-None-Match", etag) }
            .build()

        val future = client.sendAsync(
            request,
            HttpResponse.BodyHandlers.limiting(HttpResponse.BodyHandlers.ofByteArray(), RepoParser.MAX_FILE_BYTES.toLong()),
        )

        try {
            val response = future.get(25, TimeUnit.SECONDS)
            return RepoResponse(response.statusCode(), response.body().toString(Charsets.UTF_8), response.headers().firstValue("ETag").orElse(null))
        } finally {
            // Interrupting the coroutine's worker also cancels the underlying HTTP request.
            if (!future.isDone) future.cancel(true)
        }
    }
}
