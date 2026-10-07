package github.businessdirt.eurybium.api.minecraft.chat

import net.minecraft.client.multiplayer.chat.GuiMessage
import net.minecraft.network.chat.Component
import java.util.Collections
import java.util.IdentityHashMap

/** Client-thread bookkeeping bounded by HUD history and a small session-wide send-once set. */
internal class ChatMessageState(private val onceCapacity: Int = 1000) {
    private val owned = IdentityHashMap<Component, Int?>()
    private val sentOnce = linkedSetOf<Component>()

    fun wasSentOnce(message: Component): Boolean = message in sentOnce

    fun remember(message: Component, messageId: Int?, once: Boolean, history: List<GuiMessage>) {
        prune(history)
        owned[message] = messageId
        if (once) {
            sentOnce.add(message)
            while (sentOnce.size > onceCapacity) sentOnce.remove(sentOnce.first())
        }
    }

    fun shouldReplace(existing: Component, replacement: Component, id: Int?, sameMessage: Boolean): Boolean =
        owned.containsKey(existing) && ((id != null && owned[existing] == id) || (sameMessage && existing.string == replacement.string))

    /** Removes tracking when a message leaves the HUD, including explicit deletion. */
    fun prune(history: List<GuiMessage>) {
        // Identity matters: a server message with identical text must never be treated as ours.
        val visible = Collections.newSetFromMap(IdentityHashMap<Component, Boolean>())
        history.forEach { visible.add(it.content()) }
        owned.keys.retainAll(visible)
    }

    fun isOwn(message: Component): Boolean = owned.containsKey(message)

    fun clear() { owned.clear(); sentOnce.clear() }
}
