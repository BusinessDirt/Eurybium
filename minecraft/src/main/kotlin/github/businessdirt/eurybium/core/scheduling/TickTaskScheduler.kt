package github.businessdirt.eurybium.core.scheduling

import java.util.PriorityQueue
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Accepts submissions from any thread and executes due tasks on the thread calling [runTick].
 *
 * [timeSource] supplies monotonic deadlines and can be replaced with a test clock.
 * Tick processing belongs to the client thread; only queue operations are synchronized.
 */
internal class TickTaskScheduler(
    private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    private val onError: (String?, Throwable) -> Unit,
) {
    private data class Entry(
        val deadline: ComparableTimeMark,
        val sequence: Long,
        val task: ScheduledTask<*>,
    )

    private val lock = Any()
    private var sequence = 0L

    // Equal deadlines retain submission order, even when several threads enqueue work.
    private val queue = PriorityQueue(compareBy<Entry> { it.deadline }.thenBy { it.sequence })

    /** Queues a task with a finite, non-negative delay and returns its cancellation/result handle. */
    fun <T> schedule(duration: Duration, label: String? = null, block: () -> T): ScheduledTask<T> {
        require(duration.isFinite() && !duration.isNegative()) {
            "Delay must be finite and non-negative"
        }

        val task = ScheduledTask(label, block, onError)

        synchronized(lock) {
            queue.add(Entry(timeSource.markNow() + duration, sequence++, task))
        }

        return task
    }

    /** Collects one batch of due work, then executes it without holding the submission lock. */
    fun runTick() {
        val ready = synchronized(lock) {
            val now = timeSource.markNow()

            buildList {
                while (queue.isNotEmpty() && queue.peek().deadline <= now) {
                    add(queue.remove().task)
                }
            }
        }

        // Collect first so callbacks can enqueue safely without extending this batch forever.
        // Work submitted by a callback is picked up by a later tick.
        ready.forEach { it.execute() }
    }
}
