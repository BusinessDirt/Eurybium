package github.businessdirt.eurybium.api.minecraft.text

import github.businessdirt.eurybium.core.scheduling.ClientTasks
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/** Helpers for creating plain text components and delivering them on the client thread. */
object ComponentExtensions {

    /** Creates a literal component, then applies the optional style/content initializer. */
    fun String.asComponent(init: MutableComponent.() -> Unit = {}): MutableComponent =
        Component.literal(this).also(init)

    /** Delivers this component as a system message; does nothing if the player is unavailable at execution. */
    fun Component.addToChat() = ClientTasks.runOrNextTick {
        Minecraft.getInstance().player?.sendSystemMessage(this)
    }
}
