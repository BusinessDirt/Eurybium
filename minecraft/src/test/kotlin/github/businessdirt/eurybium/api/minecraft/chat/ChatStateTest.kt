package github.businessdirt.eurybium.api.minecraft.chat

import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.asComponent
import net.minecraft.client.multiplayer.chat.GuiMessage
import net.minecraft.client.multiplayer.chat.GuiMessageSource
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ChatStateTest {
    private fun gui(message: Component) = GuiMessage(0, message, null, GuiMessageSource.SYSTEM_CLIENT, null)

    @Test
    fun `replacement IDs and same-text replacement affect only owned message identities`() {
        val state = ChatMessageState()
        val owned = "same".asComponent()
        val server = "same".asComponent()
        state.remember(owned, 7, false, listOf(gui(owned), gui(server)))
        assertTrue(state.shouldReplace(owned, "different".asComponent(), 7, false))
        assertFalse(state.shouldReplace(owned, "different".asComponent(), 8, false))
        assertTrue(state.shouldReplace(owned, server, null, true))
        assertFalse(state.shouldReplace(server, owned, 7, true))
        state.prune(emptyList())
        assertFalse(state.isOwn(owned))
    }

    @Test
    fun `send-once state is bounded and is cleared with the session`() {
        val state = ChatMessageState(2)
        val texts = listOf("one", "two", "three").map { it.asComponent() }
        for (text in texts) state.remember(text, null, true, listOf(gui(text)))
        assertFalse(state.wasSentOnce(texts[0]))
        assertTrue(state.wasSentOnce("two".asComponent()))
        assertTrue(state.wasSentOnce("three".asComponent()))
        assertFalse(state.isOwn(texts[1]))
        state.clear()
        assertFalse(state.wasSentOnce(texts[2]))
    }

    @Test
    fun `repeated clickable text can replace an earlier message with a different action token`() {
        val state = ChatMessageState()
        val first = "click".asComponent { style = style.withClickEvent(ClickEvent.RunCommand("/first")) }
        val second = "click".asComponent { style = style.withClickEvent(ClickEvent.RunCommand("/second")) }
        state.remember(first, null, false, listOf(gui(first)))
        assertTrue(state.shouldReplace(first, second, null, true))
    }

    @Test
    fun `one-time callbacks are claimed before execution and expired callbacks cannot run`() {
        var clock = 10L
        val registry = ChatActionRegistry({ clock })
        var calls = 0
        val once = registry.register(20, true) { calls++ }
        val callback = assertNotNull(registry.claim(once))
        assertNull(registry.claim(once))
        callback()
        assertEquals(1, calls)
        val repeated = registry.register(20, false) { calls++ }
        assertNotNull(registry.claim(repeated))()
        assertNotNull(registry.claim(repeated))()
        clock = 20
        assertNull(registry.claim(repeated))
        assertNull(registry.claim("unknown"))
    }

    @Test
    fun `action retention is bounded and clearing invalidates old tokens`() {
        val registry = ChatActionRegistry({ 0 }, 2)
        val oldest = registry.register(100, false) {}
        registry.register(100, false) {}
        val newest = registry.register(100, false) {}
        assertNull(registry.claim(oldest))
        assertNotNull(registry.claim(newest))
        registry.clear()
        assertNull(registry.claim(newest))
    }

    @Test
    fun `owned expired or malformed native click IDs are consumed without server dispatch`() {
        val owned = ClickEvent.Custom(Identifier.parse("eurybium:chat_action"), Optional.empty())
        val other = ClickEvent.Custom(Identifier.parse("another:action"), Optional.empty())
        assertTrue(ChatAPI.handleCustomClick(owned))
        assertFalse(ChatAPI.handleCustomClick(other))
        assertFalse(ChatAPI.handleCustomClick(ClickEvent.RunCommand("/help")))
    }

    @Test
    fun `outgoing queue starts immediately then respects manual sends and 300 ms boundaries`() {
        var clock = 0L
        val queue = ChatSendQueue { clock }
        queue.enqueue("first")
        assertEquals("first", queue.poll())
        queue.recordSent()
        queue.enqueue("second")
        assertEquals(600.milliseconds, queue.estimateDelay())
        clock = 299.milliseconds.inWholeNanoseconds
        assertNull(queue.poll())
        clock = 300.milliseconds.inWholeNanoseconds
        assertEquals("second", queue.poll())
        queue.recordSent()
        queue.enqueue("third")
        clock = 400.milliseconds.inWholeNanoseconds
        queue.recordSent() // A manual player message also postpones the queued message.
        clock = 699.milliseconds.inWholeNanoseconds
        assertNull(queue.poll())
        clock = 700.milliseconds.inWholeNanoseconds
        assertEquals("third", queue.poll())
    }

    @Test
    fun `disconnect clears pending outgoing messages and rate timing`() {
        val queue = ChatSendQueue { 0 }
        queue.recordSent()
        queue.enqueue("old server")
        queue.clear()
        assertNull(queue.poll())
        assertEquals(0.milliseconds, queue.estimateDelay())
        queue.enqueue("new server")
        assertEquals("new server", queue.poll())
    }
}
