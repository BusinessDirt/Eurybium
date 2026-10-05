package github.businessdirt.eurybium.api.minecraft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.utils.ChatUtils.addToChat
import github.businessdirt.eurybium.core.utils.ChatUtils.asComponent

object Chat {

    fun debug(message: String) {
        if (EurybiumMod.config.dev.debug.enabled) internalChat(message)
    }

    private fun internalChat(
        message: String,
        replaceSameMessage: Boolean = false,
        messageId: Int? = null,
    ): Boolean {
        val text = message.asComponent()
        text.addToChat()

        return true
    }
}
