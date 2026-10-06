package github.businessdirt.eurybium.api.minecraft.text

import github.businessdirt.eurybium.core.scheduling.ClientTasks
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object ComponentExtensions {

    fun String.asComponent(init: MutableComponent.() -> Unit = {}): MutableComponent =
        Component.literal(this).also(init)

    fun Component.addToChat() = ClientTasks.runOrNextTick {
        Minecraft.getInstance().player?.sendSystemMessage(this)
    }
}
