package github.businessdirt.eurybium.api.events

import org.junit.jupiter.api.Test
import kotlin.test.*

class EventInvokerFactoryTest {

    class Event : EurybiumEvent()

    class Listener {
        var calls = 0

        private fun zero() { calls++ }

        private fun one(event: Event) { calls++ }

        fun text(value: String) {}

        fun returning(event: Event): Int = 1

        fun zeroFunction() = this::zero

        fun oneFunction() = this::one
    }

    @Test
    fun `private zero and single argument methods bind to their owner`() {
        val listener = Listener()
        EventInvokerFactory.createZeroParameterEventConsumer(listener, listener.zeroFunction()).accept(Event())
        EventInvokerFactory.createSingleParameterEventConsumer(listener, listener.oneFunction()).accept(Event())
        assertEquals(2, listener.calls)
    }

    @Test
    fun `invalid owner return type event type and arity are rejected during registration`() {
        val listener = Listener()
        for ((owner, function) in listOf(
            Any() to listener.oneFunction(),
            listener to listener.zeroFunction(),
            listener to Listener::text,
            listener to Listener::returning,
        )) {
            val error = assertFailsWith<InvalidConsumerException> {
                EventInvokerFactory.createSingleParameterEventConsumer(owner, function)
            }
            assertIs<IllegalArgumentException>(error.cause)
        }
        assertFailsWith<InvalidRunnableException> {
            EventInvokerFactory.createZeroParameterEventConsumer(listener, listener.oneFunction())
        }
    }
}
