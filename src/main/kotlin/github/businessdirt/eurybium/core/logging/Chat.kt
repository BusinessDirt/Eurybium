package github.businessdirt.eurybium.core.logging

import github.businessdirt.eurybium.core.logging.ChatFormatter.newColoredInfoChatMessage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.MessageIndicator
import net.minecraft.network.message.MessageSignatureData
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.Style
import net.minecraft.text.Text

object Chat {
    /**
     * Sends a message to the [net.minecraft.client.gui.hud.ChatHud] and optionally replaces an old message if
     * the passed [MessageSignatureData] is not null
     * @param message the message to be sent
     * @param id the id of the message to be removed
     */
    fun chat(message: String, id: MessageSignatureData? = null) = addOrReplaceMessage(Text.of { message }, id)

    /**
     * Sends a message to the user that they can click and run an action
     * @param message The message to be sent
     * @param hover The string to be shown when the message is hovered
     * @param prefix The color that the prefix should be
     * @param id optional - Replace an old message with this new message if they are identical
     * @param onClick The runnable to be executed when the message is clicked
     */
    fun clickableChat(
        message: String,
        onClick: () -> Unit,
        hover: String = "§eClick here!",
        prefix: String = "§e",
        id: MessageSignatureData? = null,
    ) {
        val rawText = newColoredInfoChatMessage(prefix, message)
        val clickable: Text = Text.literal(rawText).setStyle(Style.EMPTY.withClickEvent {
            onClick()
            null
        }.withHoverEvent(ShowText(Text.of(hover))))

        addOrReplaceMessage(clickable, id)
    }

    /**
     * Sends a message to the user that they can click and run a command
     * @param message The message to be sent
     * @param hover The message to be shown when the message is hovered
     * @param prefix Color that the prefix should be
     */
    fun hoverableChat(
        message: String,
        hover: List<String>,
        prefix: String = "§e"
    ) {
        val rawText = newColoredInfoChatMessage(prefix, message)
        val hoverable: Text = Text.literal(rawText).setStyle(
            Style.EMPTY.withHoverEvent(
                ShowText(Text.of(hover.joinToString("\n")))
            )
        )

        addOrReplaceMessage(hoverable, null)
    }

    private fun addOrReplaceMessage(message: Text, id: MessageSignatureData? = null) {
        val instance = MinecraftClient.getInstance()
        instance.execute {
            if (id != null) {
                instance.inGameHud.chatHud.removeMessage(id)
                instance.inGameHud.chatHud.addMessage(message, id, MessageIndicator.system())
                return@execute
            }
            instance.inGameHud.chatHud.addMessage(message)
        }
    }
}
