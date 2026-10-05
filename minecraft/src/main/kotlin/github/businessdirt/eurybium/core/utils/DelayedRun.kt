package github.businessdirt.eurybium.core.utils

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import github.businessdirt.eurybium.core.utils.CollectionUtils.drainTo
import github.businessdirt.eurybium.events.minecraft.TickEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.client.Minecraft
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration

@EurybiumModule
object DelayedRun {

    private val tasks = mutableListOf<Pair<() -> Any?, SimpleTimeMark>>()
    private val futureTasks = ConcurrentLinkedQueue<Pair<() -> Any?, SimpleTimeMark>>()

    /**
     * Runs [runnable] at the end of the next game tick after [duration] has passed,
     * always on the main thread.
     */
    fun runDelayed(duration: Duration, label: String?, runnable: Runnable): SimpleTimeMark {
        val time = SimpleTimeMark.now() + duration
        futureTasks.add((runnable::run).withErrorHandling("DelayedRun.runDelayed", label) to time)
        return time
    }

    fun runDelayed(duration: Duration, runnable: Runnable): SimpleTimeMark = runDelayed(duration, null, runnable)

    /**
     * Runs [block], reporting a crash instead of letting it escape.
     * [source] names the scheduling method the task came from, [label] the caller's own description of the task.
     */
    private fun runWithErrorHandling(source: String, label: String?, block: () -> Any?) {
        try {
            block()
        } catch (e: Throwable) {
            EurybiumMod.logger.atError().withThrowable(e)
                .log("Delayed task crashed while executing: ${e.message}. label=$label, source=$source")
        }
    }

    fun <T> runDelayedReturning(duration: Duration, label: String?, block: () -> T): Pair<SimpleTimeMark, () -> T> {
        val time = SimpleTimeMark.now() + duration
        futureTasks.add(block.withErrorHandling("DelayedRun.runDelayedReturning", label) to time)
        return time to block
    }

    fun <T> runDelayedReturning(duration: Duration, block: () -> T): Pair<SimpleTimeMark, () -> T> =
        runDelayedReturning(duration, null, block)

    /**
     * Schedules a task via Minecraft's internal scheduler, which runs it on the main thread
     * at the start of the next game tick. The exact point relative to SkyHanni's own event
     * handlers is not guaranteed.
     */
    fun runOnNextMinecraftTick(label: String?, runnable: Runnable) =
        Minecraft.getInstance().schedule(runnable.withErrorHandling("DelayedRun.runOnNextMinecraftTick", label))

    @JvmStatic
    fun runOnNextMinecraftTick(runnable: Runnable) = runOnNextMinecraftTick(null, runnable)

    /**
     * Runs at the end of the next game tick, after all other event handlers have processed.
     * Unlike [runOnNextMinecraftTick], this goes through SkyHanni's own tick handler at [HandleEvent.LOWEST]
     * priority, guaranteeing that all event handlers for the current tick have finished first.
     * Use this when the task reads state that other handlers (e.g. chat handlers) may still
     * modify during the current tick.
     */
    fun runAfterCurrentTickEvents(label: String?, runnable: Runnable) =
        futureTasks.add((runnable::run).withErrorHandling("DelayedRun.runAfterCurrentTickEvents", label) to SimpleTimeMark.farPast())

    @JvmStatic
    fun runAfterCurrentTickEvents(runnable: Runnable) = runAfterCurrentTickEvents(null, runnable)

    /**
     * Runs [runnable] now if we are on the main thread, otherwise schedules it for the start of the
     * next game tick, same as [runOnNextMinecraftTick].
     */
    fun runOrNextTick(label: String?, runnable: Runnable) =
        Minecraft.getInstance().execute(runnable.withErrorHandling("DelayedRun.runOrNextTick", label))

    fun runOrNextTick(runnable: Runnable) = runOrNextTick(null, runnable)

    /**
     * Wraps [this] so that a crash is reported instead of taking down the game.
     * Minecraft's own executor has no error handling, so anything scheduled on it needs this.
     */
    private fun Runnable.withErrorHandling(source: String, label: String?) = Runnable {
        runWithErrorHandling(source, label) { run() }
    }

    /**
     * Wraps [this] the same way the [Runnable] variant does, for tasks stored as a function type.
     */
    internal fun (() -> Any?).withErrorHandling(source: String, label: String?): () -> Unit = {
        runWithErrorHandling(source, label, this)
    }

    @HandleEvent(eventType = TickEvent::class, priority = HandleEvent.LOWEST)
    private fun onTickEvent() {
        tasks.removeIf { (block, time) ->
            val inPast = time.isInPast()
            if (inPast) block()
            inPast
        }

        futureTasks.drainTo(tasks)
    }
}
