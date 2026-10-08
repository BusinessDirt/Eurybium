package github.businessdirt.eurybium.core.scheduling

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.events.minecraft.TickEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.client.Minecraft
import kotlin.time.Duration

/**
 * Schedules work on the Minecraft client thread and returns a cancellable [ScheduledTask].
 *
 * Tick-based delays use a monotonic clock, so system clock adjustments do not affect them.
 * Ordinary failures are logged and retained in the task's result; cancellation does not
 * interrupt the client thread or undo work that has already started.
 */
@EurybiumModule
object ClientTasks {
    private fun reportFailure(label: String?, failure: Throwable) {
        EurybiumMod.logger.atError().withThrowable(failure)
            .log("Delayed task failed: ${label ?: "unnamed task"}")
    }

    private val scheduler = TickTaskScheduler(onError = ::reportFailure)

    /**
     * Runs [block] in the final-priority handler of the first tick at or after [duration].
     *
     * A zero delay is eligible for the current tick if its task batch has not been collected.
     * [label] identifies the task in failure logs.
     *
     * @throws IllegalArgumentException if [duration] is negative or infinite.
     */
    fun runDelayed(duration: Duration, label: String? = null, block: () -> Unit): ScheduledTask<Unit> =
        runDelayedReturning(duration, label, block)

    /**
     * Schedules a value-producing [block] with the same timing as [runDelayed].
     *
     * The returned handle stores the result of one execution. Reading [ScheduledTask.result]
     * never invokes [block] again and returns null while the task is pending.
     *
     * @throws IllegalArgumentException if [duration] is negative or infinite.
     */
    fun <T> runDelayedReturning(duration: Duration, label: String? = null, block: () -> T): ScheduledTask<T> =
        scheduler.schedule(duration, label, block)

    /**
     * Enqueues [block] on Minecraft's client executor, including calls from the client thread.
     *
     * The executor controls when the work runs; its ordering relative to mod tick handlers
     * is not guaranteed. Use [runAfterCurrentTickEvents] for the mod's final-priority batch.
     */
    fun runOnNextMinecraftTick(label: String? = null, block: () -> Unit): ScheduledTask<Unit> =
        submit(label, block) { Minecraft.getInstance().schedule(it) }

    /**
     * Runs [block] in the next available final-priority tick batch.
     *
     * Tasks submitted before batch collection may run during the current tick. Tasks
     * submitted by a running batch wait until a later tick, preventing recursive draining.
     */
    fun runAfterCurrentTickEvents(label: String? = null, block: () -> Unit): ScheduledTask<Unit> =
        runDelayed(Duration.ZERO, label, block)

    /**
     * Runs [block] immediately on the client thread, or enqueues it from another thread.
     *
     * This may execute before the method returns. Use [runOnNextMinecraftTick] when work
     * must always be deferred rather than executed inline.
     */
    fun runOrNextTick(label: String? = null, block: () -> Unit): ScheduledTask<Unit> =
        runOrNextTickReturning(label, block)

    /** Value-producing variant of [runOrNextTick], with a stored success or failure. */
    fun <T> runOrNextTickReturning(label: String? = null, block: () -> T): ScheduledTask<T> =
        submit(label, block) { Minecraft.getInstance().execute(it) }

    private fun <T> submit(label: String?, block: () -> T, enqueue: (Runnable) -> Unit): ScheduledTask<T> {
        val task = ScheduledTask(label, block, ::reportFailure)

        try {
            enqueue(Runnable(task::execute))
        } catch (failure: Exception) {
            // Submission can fail before execution; settle the handle instead of leaving it pending.
            task.reject(failure)
        }

        return task
    }

    @HandleEvent(events = [ TickEvent::class ], priority = Int.MAX_VALUE)
    private fun onTickEvent() = scheduler.runTick()
}
