package github.businessdirt.eurybium.core.logging

import github.businessdirt.eurybium.utils.Reference
import org.apache.logging.log4j.message.FormattedMessage

@Suppress("unused")
object ChatFormatter {
    private var infoChatPrefix: String? = null
    private var debugChatPrefix: String? = null
    private var errorChatPrefix: String? = null

    fun initialize() {
        infoChatPrefix = String.format("[%s] ", Reference.MOD_NAME)
        debugChatPrefix = String.format("[%s Debug] §7", Reference.MOD_NAME)
        errorChatPrefix = String.format("[%s %s for %s] ", Reference.MOD_NAME, Reference.MOD_VERSION, Reference.MC_VERSION)
    }

    fun newFormattedMessage(message: String): String =
        FormattedMessage(message).getFormattedMessage()

    fun newInfoChatMessage(message: String): String =
        newFormattedMessage("§e$infoChatPrefix$message")

    fun newColoredInfoChatMessage(prefix: String, message: String): String =
        newFormattedMessage(prefix + infoChatPrefix + message)

    fun newDebugChatMessage(message: String): String =
        newFormattedMessage("§e$debugChatPrefix$message")

    fun newColoredDebugChatMessage(prefix: String, message: String): String =
        newFormattedMessage(prefix + debugChatPrefix + message)

    fun newErrorChatMessage(message: String): String =
        newFormattedMessage("§c$errorChatPrefix$message")

    fun newColoredErrorChatMessage(prefix: String, message: String): String =
        newFormattedMessage(prefix + errorChatPrefix + message)
}
