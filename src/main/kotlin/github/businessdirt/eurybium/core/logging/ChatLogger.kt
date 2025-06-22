package github.businessdirt.eurybium.core.logging

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.core.logging.ChatFormatter.newDebugChatMessage
import github.businessdirt.eurybium.core.logging.ChatFormatter.newErrorChatMessage
import github.businessdirt.eurybium.core.logging.ChatFormatter.newInfoChatMessage
import net.minecraft.network.message.MessageSignatureData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.reflect.KMutableProperty0


/**
 * Creates a chat logger that logs to a [Logger] and the in-game chat
 *
 * @param clazz the class, which will be used to instantiate the logger
 * @param debug an optional check for debug messages to be sent to the in-game chat.
 * This will not affect the normal log
 */
class ChatLogger() {
    private var logger: Logger? = null
    private var debug: KMutableProperty0<Boolean>? = null

    /**
     * Initializes the logger
     * @param clazz the class, which will be used to instantiate the logger
     * @param debug an optional check for debug messages to be sent to the in-game chat. This will not affect the normal log
     */
    fun initialize(clazz: Class<*>, debug: KMutableProperty0<Boolean>?) {
        this.logger = LoggerFactory.getLogger(clazz)
        this.debug = debug
    }

    private fun log(
        level: Level,
        formattedChatMessage: String,
        message: String,
        vararg args: Any?,
        onlySendOnce: Boolean = false,
        id: MessageSignatureData? = null,
    ) {
        if (onlySendOnce && !MESSAGES_THAT_ARE_ONLY_SENT_ONCE.add(formattedChatMessage)) return
        this.logger!!.atLevel(level).log(message, *args)

        if (getMinecraft().inGameHud == null || getMinecraft().inGameHud.chatHud == null) return
        Chat.chat(formattedChatMessage, id)
    }

    /**
     * Logs an info message to the chat and the log
     * @param message The message
     * @param args The format arguments
     * @param onlySendOnce Whether to only send this message only once
     * @param messageId optional id to remove an old message
     */
    fun info(message: String, vararg args: Any?, onlySendOnce: Boolean = false, messageId: MessageSignatureData? = null) {
        val formattedChatMessage = newInfoChatMessage(message, *args)
        this.log(Level.INFO, formattedChatMessage, message, args, onlySendOnce, messageId)
    }

    /**
     * Logs a debug message to the chat and the log
     * @param message The message
     * @param args The format arguments
     * @param messageId optional id to remove an old message
     */
    fun debug(message: String, vararg args: Any?, messageId: MessageSignatureData? = null) {
        if (this.debug == null || this.debug!!.get()) {
            val formattedChatMessage = newDebugChatMessage(message, *args)
            this.log(Level.DEBUG, formattedChatMessage, message, args, false, messageId)
        }
    }

    /**
     * Logs an error message to the chat and the log
     * @param message The message
     * @param args The format arguments
     * @param messageId optional id to remove an old message
     */
    fun error(message: String, vararg args: Any?, messageId: MessageSignatureData? = null) {
        val formattedChatMessage = newErrorChatMessage(message, *args)
        this.log(Level.ERROR, formattedChatMessage, message, args, false, messageId)
    }


    fun warn(message: String, vararg args: Any?) {
        this.logger!!.warn(message, *args)
    }

    /**
     * Logs an empty line
     */
    fun emptyInfo() {
        this.info("")
    }

    /**
     * Logs an empty line
     */
    fun emptyDebug() {
        this.debug("")
    }

    /**
     * Logs an empty line
     */
    fun emptyError() {
        this.error("")
    }

    companion object {
        private val MESSAGES_THAT_ARE_ONLY_SENT_ONCE: MutableSet<String?> = HashSet<String?>()
    }
}
