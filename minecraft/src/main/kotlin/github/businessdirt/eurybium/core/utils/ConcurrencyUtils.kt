package github.businessdirt.eurybium.core.utils

import github.businessdirt.eurybium.EurybiumMod
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object ConcurrencyUtils {
    private val globalJob: Job = Job(null)
    private val coroutineScope = CoroutineScope(
        CoroutineName("Eurybium") + SupervisorJob(globalJob),
    )

    /**
     * Launch an IO coroutine with a lock on the provided mutex.
     * This coroutine will catch any exceptions thrown by the provided function.
     * @param mutex The mutex to lock during the execution of the block.
     * @param block The suspend function to execute within the IO context.
     */
    fun launchIOWithMutex(
        name: String,
        mutex: Mutex,
        timeout: Duration = 10.seconds,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launch("launchIOWithMutex $name", timeout) {
        mutex.withLock {
            withContext(Dispatchers.IO, block)
        }
    }

    /**
     * Launch an IO coroutine in the current scope.
     * This coroutine will catch any exceptions thrown by the provided function.
     * @param block The suspend function to execute within the IO context.
     */
    fun launchIO(
        name: String,
        timeout: Duration = 10.seconds,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launch("launchIO $name", timeout) {
        withContext(Dispatchers.IO, block)
    }

    /**
     * Launches a coroutine in the current scope.
     * This coroutine will catch any exceptions thrown by the provided function.
     * The function provided here must not rely on the CoroutineScope's context.
     */
    fun launchNoScope(
        name: String,
        timeout: Duration = 10.seconds,
        block: suspend () -> Unit,
    ): Job = launch("launchNoScope $name", timeout) { block() }

    /**
     * Launches a coroutine in the current scope.
     * This coroutine will catch any exceptions thrown by the provided function.
     * @param function The suspend function to execute in the coroutine.
     */
    @OptIn(InternalCoroutinesApi::class)
    fun launch(name: String,
               timeout: Duration = 10.seconds,
               function: suspend CoroutineScope.() -> Unit
    ): Job = coroutineScope.launch {
        val mainJob = launch {
            try {
                function()
            } catch (e: CancellationException) {
                // Don't notify the user about cancellation exceptions - these are to be expected at times
                val jobState = coroutineContext[Job]?.toString() ?: "unknown job"
                val cancellationCause = coroutineContext[Job]?.getCancellationException()
                EurybiumMod.logger.debug(
                    "Job {} was cancelled with cause: {}\n {}",
                    jobState,
                    cancellationCause,
                    e.stackTraceToString()
                )
            } catch (e: Throwable) {
                EurybiumMod.logger.atError().withThrowable(e).log("Asynchronous exception caught.")
            }
        }

        if (timeout != Duration.INFINITE && timeout != Duration.ZERO) {
            launch {
                delay(timeout)
                if (mainJob.isActive) {
                    EurybiumMod.logger.error("Coroutine timed out: The coroutine '$name' took longer than the specified timeout of $timeout (timeout=$timeout, name=$name)")
                    mainJob.cancel(CancellationException("Coroutine $name timed out after $timeout"))
                }
            }
        }
    }
}
