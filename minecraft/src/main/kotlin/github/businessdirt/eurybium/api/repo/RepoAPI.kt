package github.businessdirt.eurybium.api.repo

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.config.manager.ConfigManager
import github.businessdirt.eurybium.core.concurrency.BackgroundTasks
import github.businessdirt.eurybium.data.repo.HttpRepoTransport
import github.businessdirt.eurybium.data.repo.RepoCache
import github.businessdirt.eurybium.data.repo.RepoClient
import github.businessdirt.eurybium.data.repo.RepoSnapshot
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.ModShutdownEvent
import github.businessdirt.eurybium.events.PostModInitializationEvent
import github.businessdirt.eurybium.events.RepoUpdateEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runInterruptible
import net.minecraft.network.chat.Component
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/** Cached repository data with background refreshes and atomic client-thread publication. */
@EurybiumModule
object RepoAPI {
    private val publication = RepoPublisher({ task -> getMinecraft().execute(task) }) { event -> event.post() }
    val snapshot: RepoSnapshot get() = publication.snapshot

    @Volatile
    var lastFailure: String? = null
        private set

    private val busy = AtomicBoolean(false)
    @Volatile private var stopped = false
    private var updateJob: Job? = null
    private var pollingJob: Job? = null
    private val client by lazy {
        RepoClient(HttpRepoTransport(), RepoCache(File(ConfigManager.configDirectory, "repo/cache.json"))) { failure ->
            EurybiumMod.logger.warn("Could not save the repository cache", failure)
        }
    }

    @HandleEvent
    fun onPostModInitialization(event: PostModInitializationEvent) {
        startUpdate(restoreCache = true)
        pollingJob = BackgroundTasks.launch("repo-poll", timeout = Duration.INFINITE) {
            while (isActive) {
                delay(1.hours)
                refresh()
            }
        }
    }

    /** Requests one conditional refresh. Returns false when an update is already running or shutdown began. */
    fun refresh(): Boolean = startUpdate(restoreCache = false)

    // Pair job registration with shutdown so the polling thread cannot start an uncancelled job during exit.
    @Synchronized
    private fun startUpdate(restoreCache: Boolean): Boolean {
        if (stopped || !busy.compareAndSet(false, true)) return false
        updateJob = BackgroundTasks.launchIO("repo-update", timeout = 3.minutes) {
            try {
                if (restoreCache) {
                    try {
                        runInterruptible { client.loadCache() }?.let { publish(it, RepoUpdateEvent.Source.CACHE) }
                    } catch (failure: Exception) {
                        if (failure is kotlin.coroutines.cancellation.CancellationException) throw failure
                        EurybiumMod.logger.warn("Repository cache is unusable; trying a fresh download", failure)
                    }
                }
                runInterruptible { client.refresh() }?.let { publish(it, RepoUpdateEvent.Source.NETWORK) }
                lastFailure = null
            } catch (failure: Exception) {
                if (failure is kotlin.coroutines.cancellation.CancellationException) throw failure
                lastFailure = failure.message ?: failure.javaClass.simpleName
                throw failure
            } finally {
                busy.set(false)
            }
        }
        return true
    }

    private fun publish(candidate: RepoSnapshot, source: RepoUpdateEvent.Source) = publication.publish(candidate, source)

    @HandleEvent
    @Synchronized
    fun onModShutdown(event: ModShutdownEvent) {
        stopped = true
        publication.close()
        pollingJob?.cancel()
        updateJob?.cancel()
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("eybrepo") {
            category = CommandCategory.USERS_ACTIVE
            description = "Inspect or refresh repository data."
            callback {
                val repo = snapshot
                val status = "[Eurybium] Repo: ${repo.revision.ifEmpty { "not loaded" }}; " +
                    "${repo.patterns.size} patterns, ${repo.routes.size} routes, ${repo.nodes.size} nodes."
                val failure = lastFailure?.let { " Last refresh failed: $it" }.orEmpty()
                (context.source as FabricClientCommandSource).sendFeedback(Component.literal(status + failure))
            }
            literal("refresh") {
                callback {
                    val message = if (refresh()) "Repository refresh started." else "Repository refresh is already running or unavailable."
                    (context.source as FabricClientCommandSource).sendFeedback(Component.literal("[Eurybium] $message"))
                }
            }
        }
    }
}
