package github.businessdirt.eurybium.core.concurrency

import github.businessdirt.eurybium.EurybiumMod
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Background coroutine tasks whose ordinary failures are logged without cancelling unrelated work. */
object BackgroundTasks {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("Eurybium"))

    /** Runs [block] on the IO dispatcher while holding [mutex]; [timeout] includes time waiting for the lock. */
    fun launchIOWithMutex(
        name: String,
        mutex: Mutex,
        timeout: Duration = 10.seconds,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launch(name, timeout) {
        withContext(Dispatchers.IO) {
            mutex.withLock { block() }
        }
    }

    /** Runs blocking file/network work on the IO dispatcher with the same timeout policy as [launch]. */
    fun launchIO(
        name: String,
        timeout: Duration = 10.seconds,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launch(name, timeout) {
        withContext(Dispatchers.IO, block)
    }

    /** Convenience variant for a suspending [block] that does not need a CoroutineScope receiver. */
    fun launchNoScope(
        name: String,
        timeout: Duration = 10.seconds,
        block: suspend () -> Unit,
    ): Job = launch(name, timeout) { block() }

    /**
     * Runs [function] on the default dispatcher, including its structured child coroutines.
     *
     * Zero and infinite timeouts disable the deadline. Finite timeouts are cooperative:
     * blocking code must still support interruption or check cancellation itself.
     * Caller cancellation propagates normally; ordinary exceptions and timeouts are logged.
     *
     * @throws IllegalArgumentException if [timeout] is negative.
     */
    fun launch(
        name: String,
        timeout: Duration = 10.seconds,
        function: suspend CoroutineScope.() -> Unit,
    ): Job {
        require(!timeout.isNegative()) { "Timeout must be non-negative" }

        return scope.launch(CoroutineName(name)) {
            try {
                if (timeout == Duration.ZERO || timeout == Duration.INFINITE) {
                    function()
                } else {
                    // The deadline belongs to the work, rather than a sibling timer that keeps
                    // the returned Job alive after the work has already finished.
                    val completed = withTimeoutOrNull(timeout) {
                        function()
                        true
                    }

                    if (completed == null) {
                        EurybiumMod.logger.error("Background task '{}' timed out after {}", name, timeout)
                    }
                }
            } catch (cancelled: CancellationException) {
                // Cancellation is coroutine control flow, not a recoverable task failure.
                throw cancelled
            } catch (failure: Exception) {
                EurybiumMod.logger.atError().withThrowable(failure)
                    .log("Background task '$name' failed")
            }
        }
    }
}
