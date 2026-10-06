package github.businessdirt.eurybium.core.concurrency

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class BackgroundTasksTest {

    @Test
    fun `completed work does not wait for its timeout timer`() = runBlocking {
        val job = BackgroundTasks.launch("completed", 30.seconds) {}
        withTimeout(2.seconds) { job.join() }
        assertTrue(job.isCompleted)
    }

    @Test
    fun `timeout cancels work and runs its cleanup`() = runBlocking {
        val cleaned = CompletableDeferred<Unit>()
        val job = BackgroundTasks.launch("timeout regression", 50.milliseconds) {
            try {
                awaitCancellation()
            } finally {
                cleaned.complete(Unit)
            }
        }
        withTimeout(2.seconds) { job.join(); cleaned.await() }
    }

    @Test
    fun `caller cancellation propagates and unrelated tasks still run`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val cleaned = CompletableDeferred<Unit>()
        val job = BackgroundTasks.launch("cancel", Duration.INFINITE) {
            try {
                started.complete(Unit)
                awaitCancellation()
            } finally {
                cleaned.complete(Unit)
            }
        }
        withTimeout(2.seconds) { started.await(); job.cancelAndJoin(); cleaned.await() }
        assertTrue(job.isCancelled)
        val following = CompletableDeferred<Unit>()
        withTimeout(2.seconds) {
            BackgroundTasks.launch("following", Duration.ZERO) { following.complete(Unit) }.join()
            following.await()
        }
    }

    @Test
    fun `IO mutex tasks run on IO and release the mutex`() = runBlocking {
        val mutex = Mutex()
        val finished = CompletableDeferred<Unit>()
        val job = BackgroundTasks.launchIOWithMutex("IO mutex", mutex) {
            assertSame(Dispatchers.IO, coroutineContext[ContinuationInterceptor])
            assertTrue(mutex.isLocked)
            finished.complete(Unit)
        }
        withTimeout(2.seconds) { job.join(); finished.await() }
        assertFalse(mutex.isLocked)
    }

    @Test
    fun `negative timeout is rejected`() {
        assertFailsWith<IllegalArgumentException> { BackgroundTasks.launch("negative", -1.seconds) {} }
    }
}
