package github.businessdirt.eurybium.api.events

import kotlin.test.*

class EurybiumEventBusTest : EventBusTestFixture() {
    open class Parent : EurybiumEvent()
    class Child : Parent()
    class Other : EurybiumEvent()
    class Cancel : CancellableEurybiumEvent()

    class Subscribers(val calls: MutableList<String>) {
        @HandleEvent(priority = HandleEvent.HIGH) private fun parent(event: Parent) { calls.add("parent") }
        @HandleEvent
        private fun child(event: Child) { calls.add("child") }
        @HandleEvent(eventType = Child::class, priority = HandleEvent.HIGHEST) private fun zero() { calls.add("zero") }
        @HandleEvent(eventTypes = [Child::class, Other::class], priority = HandleEvent.LOW) private fun multiple() { calls.add("multiple") }
        @HandleEvent
        private fun base(event: EurybiumEvent) { calls.add("base") }
        @HandleEvent
        private fun cancelBase(event: CancellableEurybiumEvent) { calls.add("cancelBase") }
        fun unannotated(event: Child) { calls.add("unannotated") }
    }

    @Test
    fun `private typed and zero parameter handlers dispatch by hierarchy and priority`() {
        val calls = mutableListOf<String>()
        EurybiumEventBus.init(listOf(Subscribers(calls)))
        assertFalse(Child().post())
        assertEquals(listOf("zero", "parent", "child", "multiple"), calls)
        calls.clear()
        Other().post {}
        assertEquals(listOf("multiple"), calls)
        calls.clear()
        Parent().post()
        Cancel().post()
        assertEquals(listOf("parent"), calls)
        assertSame(EurybiumEventBus.getEventHandler(Child::class), EurybiumEventBus.getEventHandler(Child::class))
    }

    class Late(val calls: MutableList<String>) {
        @HandleEvent
        private fun parent(event: Parent) { calls.add("late") }
    }

    @Test
    fun `registration after an event was posted refreshes cached subclass handlers`() {
        assertFalse(Child().post())
        val calls = mutableListOf<String>()
        EurybiumEventBus.register(Late(calls))
        Child().post()
        assertEquals(listOf("late"), calls)
    }

    class WrongType { @HandleEvent
    fun invalid(value: String) {} }
    class TooMany { @HandleEvent
    fun invalid(first: Child, second: Child) {} }
    class Generic { @HandleEvent
    fun <T> invalid(value: T) {} }
    class Returning { @HandleEvent
    fun invalid(event: Child): Int = 1 }
    class ReturningZero { @HandleEvent(eventType = Child::class) fun invalid(): Int = 1 }

    @Test
    fun `invalid signatures and non Unit return types are rejected with method context`() {
        for (instance in listOf(WrongType(), TooMany(), Generic())) {
            val exception = assertFailsWith<ParameterException> { EurybiumEventBus.register(instance) }
            assertTrue(exception.message!!.contains("invalid"))
        }
        assertFailsWith<InvalidConsumerException> { EurybiumEventBus.register(Returning()) }
        assertFailsWith<InvalidRunnableException> { EurybiumEventBus.register(ReturningZero()) }
    }

    class Throwing(val calls: MutableList<String>, val failure: Throwable) {
        @HandleEvent(priority = HandleEvent.HIGH) private fun fail(event: Child) { throw failure }
        @HandleEvent
        private fun follow(event: Child) { calls.add("follow") }
    }

    @Test
    fun `post reports listener failures and continues to later handlers`() {
        val calls = mutableListOf<String>()
        val failure = IllegalStateException("expected test failure")
        EurybiumEventBus.register(Throwing(calls, failure))
        val errors = mutableListOf<Throwable>()
        assertFalse(Child().post { errors.add(it) })
        assertEquals(listOf<Throwable>(failure), errors)
        assertEquals(listOf("follow"), calls)
        assertFalse(Child().post())
        assertEquals(listOf("follow", "follow"), calls)
    }
}
