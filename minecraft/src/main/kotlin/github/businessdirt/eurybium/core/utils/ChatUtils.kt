package github.businessdirt.eurybium.core.utils

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object ChatUtils {

    fun String.asComponent(init: MutableComponent.() -> Unit = {}): MutableComponent =
        Component.literal(this).also(init)

    fun Component.addToChat() = DelayedRun.runOrNextTick {
        Minecraft.getInstance().player?.sendSystemMessage(this)
    }
}
