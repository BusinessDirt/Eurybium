package github.businessdirt.eurybium.core.logging

import org.apache.logging.log4j.message.FormattedMessage

object ChatFormatter {
    private var infoChatPrefix: String? = null
    private var debugChatPrefix: String? = null
    private var errorChatPrefix: String? = null

    fun initialize(clazz: Class<*>) {
        val prefix = clazz.getSimpleName()
        infoChatPrefix = String.format("[%s] ", prefix)
        debugChatPrefix = String.format("[%s Debug] §7", prefix)
        errorChatPrefix = String.format("[%s] ", prefix)
    }

    fun newFormattedMessage(message: String, vararg args: Any?): String =
        FormattedMessage(message, *args).getFormattedMessage()

    fun newInfoChatMessage(message: String, vararg args: Any?): String =
        newFormattedMessage("§e$infoChatPrefix$message", *args)

    fun newColoredInfoChatMessage(prefix: String, message: String, vararg args: Any?): String =
        newFormattedMessage(prefix + infoChatPrefix + message, *args)

    fun newDebugChatMessage(message: String, vararg args: Any?): String =
        newFormattedMessage("§e$debugChatPrefix$message", *args)

    fun newColoredDebugChatMessage(prefix: String, message: String, vararg args: Any?): String =
        newFormattedMessage(prefix + debugChatPrefix + message, *args)

    fun newErrorChatMessage(message: String, vararg args: Any?): String =
        newFormattedMessage("§c$errorChatPrefix$message", *args)

    fun newColoredErrorChatMessage(prefix: String, message: String, vararg args: Any?): String =
        newFormattedMessage(prefix + errorChatPrefix + message, *args)
}
