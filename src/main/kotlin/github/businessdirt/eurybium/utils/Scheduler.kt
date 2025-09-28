package github.businessdirt.eurybium.utils

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import kotlin.time.Duration

typealias Task = () -> Unit

@Suppress("unused")
object Scheduler {

    private val tasks = mutableListOf<Pair<() -> Any, SimpleTimeMark>>()
    private val futureTasks = ConcurrentLinkedQueue<Pair<() -> Any, SimpleTimeMark>>()

    fun schedule(delay: Duration, task: Task): SimpleTimeMark {
        val time = SimpleTimeMark.now() + delay
        futureTasks.add(task to time)
        return time
    }

    /** Runs in the next full Tick so the delay is between 50ms to 100ms**/
    fun scheduleTick(task: Task) {
        futureTasks.add(task to SimpleTimeMark.farPast())
    }

    fun checkRuns() {
        tasks.removeIf { (runnable, time) ->
            val inPast = time.isInPast()
            if (inPast) {
                try {
                    runnable()
                } catch (e: Exception) {
                    throw RuntimeException("Scheduler task crashed while executing", e)
                }
            }
            inPast
        }

        tasks.addAll(tasks)
        futureTasks.clear()
    }

    @JvmField
    val onThread = Executor {
        val mc = getMinecraft()
        if (mc.isOnThread) {
            it.run()
        } else {
            mc.send(it)
        }
    }
}