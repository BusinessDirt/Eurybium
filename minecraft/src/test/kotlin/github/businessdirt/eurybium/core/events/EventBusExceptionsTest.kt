package github.businessdirt.eurybium.core.events

import kotlin.test.*

class EventBusExceptionsTest {
    class Methods {
        fun zero() {}
        fun one(event: EurybiumEvent) {}
        fun two(first: EurybiumEvent, second: EurybiumEvent) {}
    }

    @Test
    fun `signature diagnostics include method context expected parameter counts and original cause`() {
        val cause = IllegalArgumentException("cause")
        val consumer = InvalidConsumerException(Methods::two, cause)
        assertSame(cause, consumer.cause)
        assertTrue(consumer.message!!.contains("::two("))
        assertTrue(consumer.message!!.contains("Expected parameter count of 1 but was 2"))
        val runnable = InvalidRunnableException(Methods::one, cause)
        assertSame(cause, runnable.cause)
        assertTrue(runnable.message!!.contains("Expected parameter count of 0 but was 1"))
        assertTrue(InvalidConsumerException(Methods::one, null).message!!.contains("Unknown reason"))
        assertTrue(InvalidRunnableException(Methods::zero, null).message!!.contains("Unknown reason"))
        assertTrue(MethodNotPublicException(Methods::zero).message!!.contains("::zero() is not public"))
        assertTrue(ParameterException(Methods::one, "invalid signature").message!!.endsWith("invalid signature"))
    }
}
