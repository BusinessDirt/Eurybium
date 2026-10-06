package github.businessdirt.eurybium.core.utils

import java.util.concurrent.ExecutionException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.test.*
import kotlin.time.TestTimeSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TickTaskSchedulerTest {
    private val clock = TestTimeSource()
    private val errors = mutableListOf<Pair<String?, Throwable>>()
    private val scheduler = TickTaskScheduler(clock) { label, error -> errors.add(label to error) }

    @Test
    fun `deadlines include the exact boundary and run in deadline then submission order`() {
        val calls = mutableListOf<String>()
        scheduler.schedule(2.seconds) { calls.add("later") }
        scheduler.schedule(1.seconds) { calls.add("first") }
        scheduler.schedule(1.seconds) { calls.add("second") }
        scheduler.schedule(Duration.ZERO) { calls.add("immediate") }
        scheduler.runTick()
        assertEquals(listOf("immediate"), calls)
        clock += 1.seconds
        scheduler.runTick()
        assertEquals(listOf("immediate", "first", "second"), calls)
        clock += 1.seconds
        scheduler.runTick()
        assertEquals(listOf("immediate", "first", "second", "later"), calls)
    }

    @Test
    fun `tasks queued by a running batch wait for another tick`() {
        val calls = mutableListOf<String>()
        scheduler.schedule(Duration.ZERO) {
            calls.add("outer")
            scheduler.schedule(Duration.ZERO) { calls.add("inner") }
        }
        scheduler.runTick()
        assertEquals(listOf("outer"), calls)
        scheduler.runTick()
        assertEquals(listOf("outer", "inner"), calls)
    }

    @Test
    fun `result reads do not repeat work and cancelled tasks never execute`() {
        var calls = 0
        val result = scheduler.schedule(Duration.ZERO) { ++calls }
        val cancelled = scheduler.schedule(Duration.ZERO) { ++calls }
        assertFalse(result.isDone)
        assertNull(result.result)
        assertTrue(cancelled.cancel(true))
        scheduler.runTick()
        scheduler.runTick()
        assertEquals(1, result.get())
        assertEquals(1, result.result!!.getOrThrow())
        assertEquals(1, result.get(1, TimeUnit.SECONDS))
        assertEquals(1, calls)
        assertTrue(result.isDone)
        assertFalse(result.cancel())
        assertTrue(cancelled.isCancelled)
        assertIs<CancellationException>(cancelled.result!!.exceptionOrNull())
        assertFailsWith<CancellationException> { cancelled.get() }
    }

    @Test
    fun `failed task reports its label and cause without stopping the batch`() {
        val failure = IllegalStateException("expected")
        val failed = scheduler.schedule(Duration.ZERO, "test label") { throw failure }
        val following = scheduler.schedule(Duration.ZERO) { 42 }
        scheduler.runTick()
        assertEquals(42, following.get())
        assertSame(failure, assertFailsWith<ExecutionException> { failed.get() }.cause)
        assertSame(failure, failed.result!!.exceptionOrNull())
        assertEquals(1, errors.size)
        assertEquals("test label", errors.single().first)
        assertSame(failure, errors.single().second)
    }

    @Test
    fun `negative and infinite delays are rejected`() {
        for (delay in listOf(-1.seconds, Duration.INFINITE, -Duration.INFINITE)) {
            assertFailsWith<IllegalArgumentException> { scheduler.schedule(delay) {} }
        }
    }

    @Test
    fun `worker thread submissions are safe and execute only on the ticking thread`() {
        val current = Thread.currentThread()
        val tasks = java.util.Collections.synchronizedList(mutableListOf<ScheduledTask<Thread>>())
        val workers = List(4) {
            Thread {
                repeat(50) {
                    tasks.add(scheduler.schedule(Duration.ZERO) { Thread.currentThread() })
                }
            }
        }
        workers.forEach(Thread::start)
        workers.forEach(Thread::join)
        assertTrue(tasks.none { it.isDone })
        scheduler.runTick()
        assertEquals(200, tasks.size)
        tasks.forEach { assertSame(current, it.get()) }
    }

    @Test
    fun `scheduler rejection is observable and fatal errors are not swallowed`() {
        val rejection = IllegalStateException("rejected")
        val task = ScheduledTask("rejected", { 1 }) { label, error -> errors.add(label to error) }
        task.reject(rejection)
        assertSame(rejection, assertFailsWith<ExecutionException> { task.get() }.cause)
        assertEquals("rejected", errors.single().first)
        val fatal = AssertionError("fatal")
        val fatalTask = scheduler.schedule(Duration.ZERO) { throw fatal }
        assertSame(fatal, assertFailsWith<AssertionError> { scheduler.runTick() })
        assertSame(fatal, assertFailsWith<ExecutionException> { fatalTask.get() }.cause)
    }
}
