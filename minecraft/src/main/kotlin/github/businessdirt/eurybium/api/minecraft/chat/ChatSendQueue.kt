package github.businessdirt.eurybium.api.minecraft.chat

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

/** Rate-limited outgoing messages; state belongs to the client thread and is cleared on disconnect. */
internal class ChatSendQueue(private val now: () -> Long = System::nanoTime) {
    private val queue = ArrayDeque<String>()
    private var lastSent: Long? = null
    private val delayNanos = 300.milliseconds.inWholeNanoseconds

    fun enqueue(message: String) { queue.addLast(message) }
    fun recordSent() { lastSent = now() }

    fun poll(): String? {
        if (queue.isEmpty()) return null
        if (lastSent?.let { now() - it < delayNanos } == true) return null
        return queue.removeFirst()
    }

    /** Time until a message added now could be dispatched, including already queued messages. */
    fun estimateDelay(): Duration {
        val remaining = lastSent?.let { (delayNanos - (now() - it)).coerceAtLeast(0) } ?: 0
        return (remaining + queue.size.toLong() * delayNanos).nanoseconds
    }

    fun clear() { queue.clear(); lastSent = null }
}
