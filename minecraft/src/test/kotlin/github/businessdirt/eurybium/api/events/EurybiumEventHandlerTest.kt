package github.businessdirt.eurybium.api.events

import github.businessdirt.eurybium.api.events.CancellableEurybiumEvent
import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.api.events.EurybiumEventHandler
import github.businessdirt.eurybium.api.events.EurybiumEventListener
import github.businessdirt.eurybium.api.events.HandleEvent
import java.util.function.Consumer
import kotlin.test.*

class EurybiumEventHandlerTest {
    class Event : CancellableEurybiumEvent()
    private fun listener(name: String, priority: Int = 0, receiveCancelled: Boolean = false, action: (EurybiumEvent) -> Unit) =
        EurybiumEventListener(
            name,
            Consumer(action),
            HandleEvent(priority = priority, receiveCancelled = receiveCancelled)
        )

    @Test
    fun `priority sorting is stable and cancellation skips ordinary listeners but reaches opted in listeners`() {
        val calls = mutableListOf<String>()
        val handler = EurybiumEventHandler(
            Event::class, listOf(
                listener("late", 2) { calls.add("late") },
                listener("cancel", -1) { calls.add("cancel"); (it as Event).cancel() },
                listener("cancelled", 1, true) { calls.add("cancelled") },
                listener("first", -2) { calls.add("first") },
                listener("second", -2) { calls.add("second") },
            )
        )
        assertEquals("Event", handler.name)
        assertTrue(handler.post(Event(), null))
        assertEquals(listOf("first", "second", "cancel", "cancelled"), calls)
    }

    @Test
    fun `cancellation short circuits when nobody receives cancelled events`() {
        var calls = 0
        val handler = EurybiumEventHandler(
            Event::class, listOf(
                listener("cancel") { (it as Event).cancel() },
                listener("skipped") { calls++ },
            )
        )
        assertTrue(handler.post(Event(), null))
        assertEquals(0, calls)
    }

    @Test
    fun `pre cancelled event preserves status even without listeners`() {
        val event = Event().apply { cancel() }
        assertTrue(event.isCancelled)
        assertTrue(EurybiumEventHandler(Event::class, emptyList()).post(event, null))
        assertFalse(EurybiumEventHandler(Event::class, emptyList()).post(Event(), null))
    }

    @Test
    fun `extra predicates short circuit and cancelled opt in is honored`() {
        var secondChecks = 0
        val listener = EurybiumEventListener(
            "test", Consumer {}, HandleEvent(), listOf(
                { false }, { secondChecks++; true },
            )
        )
        assertFalse(listener.shouldInvoke(Event()))
        assertEquals(0, secondChecks)
        val cancelled = Event().apply { cancel() }
        assertFalse(listener("ordinary") {}.shouldInvoke(cancelled))
        assertTrue(listener("opted in", receiveCancelled = true) {}.shouldInvoke(cancelled))
    }

    @Test
    fun `error callback receives original throwable and may abort dispatch itself`() {
        val original = IllegalArgumentException("original")
        val callbackFailure = IllegalStateException("callback")
        val handler = EurybiumEventHandler(Event::class, listOf(listener("throw") { throw original }))
        assertSame(callbackFailure, assertFailsWith<IllegalStateException> {
            handler.post(Event()) { assertSame(original, it); throw callbackFailure }
        })
    }
}
