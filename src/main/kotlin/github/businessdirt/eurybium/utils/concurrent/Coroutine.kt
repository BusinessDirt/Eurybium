package github.businessdirt.eurybium.utils.concurrent

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.utils.Reference
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

object Coroutine {

    private val globalJob: Job = Job(null)
    private val coroutineScope = CoroutineScope(
        CoroutineName(Reference.MOD_NAME) + SupervisorJob(globalJob),
    )

    /**
     * Launch an IO coroutine with a lock on the provided mutex.
     * This coroutine will catch any exceptions thrown by the provided function.
     * @param mutex The mutex to lock during the execution of the block.
     * @param block The suspend function to execute within the IO context.
     */
    fun launchIOCoroutineWithMutex(
        mutex: Mutex,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launchCoroutine {
        mutex.withLock {
            withContext(Dispatchers.IO, block)
        }
    }

    /**
     * Launch an IO coroutine in the current scope.
     * This coroutine will catch any exceptions thrown by the provided function.
     * @param block The suspend function to execute within the IO context.
     */
    fun launchIOCoroutine(block: suspend CoroutineScope.() -> Unit): Job = launchCoroutine {
        withContext(Dispatchers.IO, block)
    }

    /**
     * Launches a coroutine in the current scope.
     * This coroutine will catch any exceptions thrown by the provided function.
     * The function provided here must not rely on the CoroutineScope's context.
     * @param function The function to execute in the coroutine.
     */
    fun launchNoScopeCoroutine(function: suspend () -> Unit): Job = launchCoroutine { function() }

    /**
     * Launches a coroutine in the current scope.
     * This coroutine will catch any exceptions thrown by the provided function.
     * @param function The suspend function to execute in the coroutine.
     */
    @OptIn(InternalCoroutinesApi::class)
    fun launchCoroutine(function: suspend CoroutineScope.() -> Unit): Job = coroutineScope.launch {
        try {
            function()
        } catch (e: CancellationException) {
            // Don't notify the user about cancellation exceptions - these are to be expected at times
            val jobState = coroutineContext[Job]?.toString() ?: "unknown job"
            val cancellationCause = coroutineContext[Job]?.getCancellationException()
            EurybiumMod.logger.debug("Job $jobState was cancelled with cause: $cancellationCause", e)
        } catch (e: Throwable) {
            EurybiumMod.logger.error("Asynchronous exception caught: {}", e.message)
        }
    }
}