package github.businessdirt.eurybium.core.scheduling

import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

/**
 * Stores the outcome of one scheduled execution and supports cancellation without interruption.
 *
 * Prefer [result] for non-blocking access. Calling [get] on the client thread while the task
 * is pending can prevent the task from ever running. Cancellation prevents execution when
 * possible, but cannot roll back a block that has already begun.
 */
class ScheduledTask<T> internal constructor(
    private val label: String?,
    block: () -> T,
    private val onError: (String?, Throwable) -> Unit,
) : Future<T> {
    private val future = object : FutureTask<T>(Callable(block)) {
        fun reject(failure: Throwable) = setException(failure)

        override fun done() {
            if (isCancelled) return

            try {
                get()
            } catch (failure: ExecutionException) {
                val cause = failure.cause ?: failure
                // FutureTask captures every throwable. Keep fatal errors visible to the executor
                // rather than treating them as recoverable failures of a delayed block.
                if (cause is Error) throw cause

                onError(label, cause)
            }
        }
    }

    /**
     * The stored outcome, or null while pending. Reading this property never blocks or reruns work.
     *
     * Failures expose their original cause, and cancelled tasks contain a [CancellationException].
     * A successful nullable result is represented by a non-null [Result] containing null.
     */
    val result: Result<T>?
        get() {
            if (!isDone) return null

            return try {
                Result.success(future.get())
            } catch (failure: ExecutionException) {
                Result.failure(failure.cause ?: failure)
            } catch (cancelled: CancellationException) {
                Result.failure(cancelled)
            }
        }

    /** Attempts cancellation without interruption; returns false if the task is already settled. */
    fun cancel(): Boolean = future.cancel(false)

    /** [mayInterruptIfRunning] is deliberately ignored to protect the Minecraft client thread. */
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = cancel()

    override fun isCancelled(): Boolean = future.isCancelled

    override fun isDone(): Boolean = future.isDone

    /** Waits for completion. Use [result] instead when reading from the client thread. */
    override fun get(): T = future.get()

    /** Waits up to [timeout] in [unit]; this still blocks the calling thread. */
    override fun get(timeout: Long, unit: TimeUnit): T = future.get(timeout, unit)

    /** Executes at most once; FutureTask skips work if it is already completed or cancelled. */
    internal fun execute() = future.run()

    /** Completes a task exceptionally when its executor rejects submission. */
    internal fun reject(failure: Throwable) = future.reject(failure)
}
