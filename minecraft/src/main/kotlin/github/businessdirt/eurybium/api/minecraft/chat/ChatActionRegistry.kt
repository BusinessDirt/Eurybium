package github.businessdirt.eurybium.api.minecraft.chat

import java.util.UUID

/** Bounded local callbacks, independently testable without a running Minecraft client. */
internal class ChatActionRegistry(private val now: () -> Long = System::currentTimeMillis, private val capacity: Int = 1000) {
    private data class Action(val expiresAt: Long, val once: Boolean, val callback: () -> Unit)
    private val actions = linkedMapOf<String, Action>()

    @Synchronized
    fun register(expiresAt: Long, once: Boolean, callback: () -> Unit): String {
        cleanup()
        val id = UUID.randomUUID().toString()
        actions[id] = Action(expiresAt, once, callback)
        while (actions.size > capacity) actions.remove(actions.keys.first())
        return id
    }

    /** Claims one-time actions before execution, so failures or nested clicks cannot run them twice. */
    @Synchronized
    fun claim(id: String): (() -> Unit)? {
        val action = actions[id] ?: return null
        if (action.expiresAt <= now()) {
            actions.remove(id)
            return null
        }

        if (action.once) actions.remove(id)
        return action.callback
    }

    @Synchronized
    fun cleanup() { actions.entries.removeIf { it.value.expiresAt <= now() } }

    @Synchronized
    fun clear() = actions.clear()
}
