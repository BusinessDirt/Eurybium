package github.businessdirt.eurybium.core.logging

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.core.logging.ChatFormatter.newDebugChatMessage
import github.businessdirt.eurybium.core.logging.ChatFormatter.newErrorChatMessage
import github.businessdirt.eurybium.core.logging.ChatFormatter.newInfoChatMessage
import github.businessdirt.eurybium.utils.ClipboardUtils
import github.businessdirt.eurybium.utils.StringUtils.removeColor
import net.minecraft.network.message.MessageSignatureData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.reflect.KMutableProperty0


/**
 * Creates a chat logger that logs to a [Logger] and the in-game chat
 *
 * This will not affect the normal log
 */
@Suppress("unused")
class ChatLogger() {
    private var logger: Logger? = null
    private var debug: KMutableProperty0<Boolean>? = null

    private val breakAfter = listOf(
        "at at.hannibal2.skyhanni.config.commands.Commands\$createCommand",
        "at net.minecraftforge.fml.common.eventhandler.EventBus.post",
        "at at.hannibal2.skyhanni.mixins.hooks.NetHandlerPlayClientHookKt.onSendPacket",
        "at net.minecraft.client.main.Main.main",
        "at.hannibal2.skyhanni.api.event.EventListeners.createZeroParameterConsumer",
        "at.hannibal2.skyhanni.api.event.EventListeners.createSingleParameterConsumer",
    )

    private val replace = mapOf(
        "github.businessdirt.eurybium." to "EYB.",
        "io.moulberry.notenoughupdates." to "NEU.",
        "net.minecraft." to "MC.",
        "net.fabricmc.fabric." to "FABRIC.",
        "knot//" to "",
        "java.base/" to "",
    )

    private val replaceEntirely = mapOf(
        "github.businessdirt.eurybium.core.events.EurybiumEventBus.createConsumerFromMethod" to "<Eurybium event post>",
    )

    private val ignored = listOf(
        "at java.lang.Thread.run",
        "at java.util.concurrent.",
        "at java.lang.reflect.",
        "at net.minecraft.network.",
        "at net.minecraft.client.Minecraft.addScheduledTask(",
        "at net.fabricmc.devlaunchinjector.",
        "at io.netty.",
        "at com.google.gson.internal.",
        "at sun.reflect.",
        "at net.minecraft.launchwrapper.",
    )

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
        onlySendOnce: Boolean = false,
        id: MessageSignatureData? = null,
    ) {
        if (onlySendOnce && !MESSAGES_THAT_ARE_ONLY_SENT_ONCE.add(formattedChatMessage)) return
        this.logger!!.atLevel(level).log(message)

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
    fun info(message: String, onlySendOnce: Boolean = false, messageId: MessageSignatureData? = null) {
        val formattedChatMessage = newInfoChatMessage(message)
        this.log(Level.INFO, formattedChatMessage, message, onlySendOnce, messageId)
    }

    /**
     * Logs a debug message to the chat and the log
     * @param message The message
     * @param args The format arguments
     * @param messageId optional id to remove an old message
     */
    fun debug(message: String, messageId: MessageSignatureData? = null) {
        if (this.debug == null || this.debug!!.get()) {
            val formattedChatMessage = newDebugChatMessage(message)
            this.log(Level.DEBUG, formattedChatMessage, message, false, messageId)
        }
    }

    /**
     * Logs an error message to the chat and the log
     * @param message The message
     * @param args The format arguments
     * @param messageId optional id to remove an old message
     */
    fun error(message: String, messageId: MessageSignatureData? = null) {
        val formattedChatMessage = newErrorChatMessage(message)
        this.log(Level.ERROR, formattedChatMessage, message, false, messageId)
    }

    private fun Throwable.getCustomStackTrace(
        parent: List<String> = emptyList()
    ): List<String> = buildList {
        add("Caused by ${this@getCustomStackTrace.javaClass.name}: $message")

        for (traceElement in stackTrace) {
            val text = "\tat $traceElement"
            if (text in parent) break

            var visualText = text
            for ((from, to) in replaceEntirely) {
                if (visualText.contains(from)) {
                    visualText = to
                    break
                }
            }

            for ((from, to) in replace) {
                visualText = visualText.replace(from, to)
            }

            if (breakAfter.any { text.contains(it) }) {
                add(visualText)
                break
            }

            if (ignored.any { text.contains(it) }) continue
            add(visualText)
        }

        if (this === cause) {
            add("<Infinite recurring causes>")
            return@buildList
        }

        cause?.let {
            addAll(it.getCustomStackTrace(this))
        }
    }

    private fun String.buildFinalMessage(): String {
        if (this.last() !in ".?!") return "$this§c."
        return this
    }

    fun error(throwable: Throwable, message: String) {
        this.logger!!.error(message + throwable.stackTraceToString())
        if (getMinecraft().inGameHud == null || getMinecraft().inGameHud.chatHud == null) return

        val fullStackTrace: String = throwable.getCustomStackTrace().joinToString("\n")
        Chat.clickableChat(
            newErrorChatMessage("${message.buildFinalMessage()} Click here to copy the error into the clipboard."),
            {
                ClipboardUtils.copyToClipboard(fullStackTrace)
            },
            hover = "§cClick to copy!",
            prefix = ""
        )
    }

    fun warn(message: String) {
        this.logger!!.warn(message)
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
