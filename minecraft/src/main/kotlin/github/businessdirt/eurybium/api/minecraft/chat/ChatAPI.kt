package github.businessdirt.eurybium.api.minecraft.chat

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.PlatformActions
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.asComponent
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.componentBuilder
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.copyToClipboard
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.copyTree
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.hover
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.multiline
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.onClick
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.url
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.webUri
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import github.businessdirt.eurybium.core.scheduling.ClientTasks
import github.businessdirt.eurybium.core.scheduling.ScheduledTask
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import github.businessdirt.eurybium.events.PreModInitializationEvent
import github.businessdirt.eurybium.events.minecraft.AllowChatMessageEvent
import github.businessdirt.eurybium.events.minecraft.AllowGameMessageEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.minecraft.mixin.ChatComponentAccessor
import github.businessdirt.eurybium.processors.EurybiumModule
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.chat.GuiMessage
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaField

/** Local chat and explicit server sending, with client-thread delivery and session-scoped action state. */
@EurybiumModule
object ChatAPI {
    private val actions = ChatActionRegistry()
    private val messages = ChatMessageState()
    private val outgoing = ChatSendQueue()
    private val messageIds = AtomicInteger(1)
    private val actionId = Identifier.parse("eurybium:chat_action")
    private var deleteNext: Pair<String, (Component) -> Boolean>? = null

    var lastButtonClicked: Long = 0
        private set

    /** Debug messages respect the developer toggle and are also logged to the console. */
    fun debug(message: String, replaceSameMessage: Boolean = false) {
        if (!EurybiumMod.config.dev.debug.enabled) return
        chat("§8[Eurybium Debug] §7$message", prefix = false, replaceSameMessage = replaceSameMessage)
        consoleLog("[Debug] ${message.removeColor()}")
    }

    fun userError(message: String, replaceSameMessage: Boolean = false) =
        chat("§c[Eurybium] $message", prefix = false, replaceSameMessage = replaceSameMessage)

    fun chat(
        message: String,
        prefix: Boolean = true,
        prefixColor: String? = null,
        replaceSameMessage: Boolean = false,
        onlySendOnce: Boolean = false,
        messageId: Int? = null,
    ) = chat(message.asComponent(), prefix, legacyColor(prefixColor), replaceSameMessage, onlySendOnce, messageId)

    fun chat(
        prefix: Boolean = true,
        prefixColor: Int? = null,
        replaceSameMessage: Boolean = false,
        onlySendOnce: Boolean = false,
        messageId: Int? = null,
        builder: MutableComponent.() -> Unit,
    ) = chat(componentBuilder(builder), prefix, prefixColor, replaceSameMessage, onlySendOnce, messageId)

    /** Returns a scheduled handle; unavailable players are logged instead of consuming send-once state. */
    fun chat(
        message: Component,
        prefix: Boolean = true,
        prefixColor: Int? = null,
        replaceSameMessage: Boolean = false,
        onlySendOnce: Boolean = false,
        messageId: Int? = null,
    ): ScheduledTask<Unit> {
        val text = formatted(message, prefix, prefixColor)

        return ClientTasks.runOrNextTick("chat") {
            val minecraft = Minecraft.getInstance()
            if (minecraft.player == null) {
                consoleLog(text.string)
                return@runOrNextTick
            }

            if (onlySendOnce && messages.wasSentOnce(text)) return@runOrNextTick

            val hud = minecraft.gui.chat
            val accessor = hud as ChatComponentAccessor
            val history = accessor.eurybiumMessages
            if (history.removeIf { messages.shouldReplace(it.content(), text, messageId, replaceSameMessage) }) {
                accessor.eurybiumRefreshMessages()
            }

            hud.addClientSystemMessage(text)
            messages.remember(text, messageId, onlySendOnce, history)
        }
    }


    /** Creates an independent component tree without a running client, for builders and tests. */
    internal fun formatted(message: Component, prefix: Boolean, prefixColor: Int?): MutableComponent = componentBuilder {
        if (prefix) append(Component.literal("[Eurybium] ").withColor(prefixColor ?: ChatFormatting.YELLOW.color!!))
        append(message.copyTree())
    }

    internal fun legacyColor(value: String?): Int? {
        if (value == null) return null
        val code = value.removePrefix("§").singleOrNull()
        val color = code?.let(ChatFormatting::getByCode)?.takeIf { it.isColor }?.color
        require(color != null) { "Prefix color must be a legacy color such as §e" }
        return color
    }

    fun getUniqueMessageId(): Int = messageIds.getAndIncrement()

    /** A snapshot of HUD history; callers cannot alter the live chat list through this API. */
    val chatMessages: List<GuiMessage>
        get() = (Minecraft.getInstance().gui.chat as ChatComponentAccessor).eurybiumMessages.toList()

    internal fun isOwnMessage(component: Component): Boolean = messages.isOwn(component)

    fun clickableChat(
        message: String,
        onClick: () -> Unit,
        hover: String = "§eClick here!",
        expireAt: SimpleTimeMark = SimpleTimeMark.farFuture(),
        prefix: Boolean = true,
        prefixColor: String? = null,
        oneTimeClick: Boolean = false,
        replaceSameMessage: Boolean = false,
        messageId: Int? = null,
    ) = chat(message.asComponent {
        this.hover = hover.asComponent()
        this.onClick(expireAt, oneTimeClick, onClick)
    }, prefix, legacyColor(prefixColor), replaceSameMessage, messageId = messageId)

    fun hoverableChat(message: String, hover: List<String>, command: String? = null, prefix: Boolean = true, prefixColor: String? = null) =
        chat(message.asComponent {
            this.hover = multiline(hover)
            command?.let { style = style.withClickEvent(ClickEvent.RunCommand("/" + it.removePrefix("/"))) }
        }, prefix, legacyColor(prefixColor))

    fun clickToClipboard(message: String, lines: List<String>) = chat(message.asComponent {
        hover = multiline(lines + "" + "§eClick to copy to clipboard!")
        copyToClipboard(lines.joinToString("\n") { it.removeColor() })
    })

    fun clickableLinkChat(
        message: String,
        url: String,
        hover: String = "§eOpen $url",
        autoOpen: Boolean = false,
        prefix: Boolean = true,
        prefixColor: String? = null,
        replaceSameMessage: Boolean = false,
    ) = chat(message.asComponent {
        this.url = url
        this.hover = hover.asComponent()
    }, prefix, legacyColor(prefixColor), replaceSameMessage).also {
        if (autoOpen) ClientTasks.runOrNextTick("chat-open-link") { PlatformActions.openBrowser(webUri(url).toString()) }
    }

    /** Local Custom click IDs are intercepted before Minecraft can send a server click packet. */
    internal fun createClickAction(expireAt: SimpleTimeMark, once: Boolean, action: () -> Unit): ClickEvent {
        val token = actions.register(expireAt.toMillis(), once, action)
        return ClickEvent.Custom(actionId, Optional.of(StringTag.valueOf(token)))
    }

    /** Returns true even for expired/unknown owned tokens, keeping those clicks entirely local. */
    @JvmStatic
    fun handleCustomClick(event: ClickEvent): Boolean {
        if (event !is ClickEvent.Custom || event.id() != actionId) return false
        val token = (event.payload().orElse(null) as? StringTag)?.value() ?: return true

        ClientTasks.runOrNextTick("chat-click") {
            actions.claim(token)?.let { action ->
                lastButtonClicked = System.currentTimeMillis()
                action()
            }
        }

        return true
    }

    /** Removes matching local Eurybium messages, without deleting identical server/player messages. */
    fun deleteMessages(predicate: (Component) -> Boolean) = ClientTasks.runOrNextTick("chat-delete") {
        val accessor = Minecraft.getInstance().gui.chat as ChatComponentAccessor
        if (accessor.eurybiumMessages.removeIf { messages.isOwn(it.content()) && predicate(it.content()) }) {
            accessor.eurybiumRefreshMessages()
            messages.prune(accessor.eurybiumMessages)
        }
    }

    fun deleteMessage(messageId: Int) = deleteMessages { component -> messages.shouldReplace(component, component, messageId, false) }

    /** Consumes the next incoming chat/server-system message; action-bar updates do not consume it. */
    fun deleteNextMessage(reason: String, predicate: (Component) -> Boolean) = ClientTasks.runOrNextTick("chat-delete-next") {
        deleteNext = reason to predicate
    }

    private fun consumeNext(component: Component): Boolean {
        val pending = deleteNext ?: return false
        deleteNext = null
        val matches = pending.second(component)
        if (matches) EurybiumMod.logger.debug("Blocked next message: {}", pending.first)
        return matches
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onAllowChat(event: AllowChatMessageEvent) { if (consumeNext(event.message)) event.cancel() }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onAllowGame(event: AllowGameMessageEvent) { if (!event.overlay && consumeNext(event.message)) event.cancel() }

    /** Explicit network operation; slash-prefixed text is dispatched as a command, other text as chat. */
    fun sendMessageToServer(message: String) = ClientTasks.runOrNextTick("chat-send-server") {
        require(message.isNotBlank() && '\n' !in message && '\r' !in message && message != "/") { "Expected one nonempty chat message or command" }
        if (Minecraft.getInstance().connection == null) {
            consoleLog("Cannot send a server message while disconnected.")
            return@runOrNextTick
        }
        outgoing.enqueue(message)
        dispatchNext()
    }

    private fun dispatchNext() {
        val connection = Minecraft.getInstance().connection ?: return
        val message = outgoing.poll() ?: return
        if (message.startsWith('/')) connection.sendCommand(message.drop(1)) else connection.sendChat(message)
        outgoing.recordSent()
    }

    fun getTimeWhenNewlyQueuedMessageGetsExecuted(): SimpleTimeMark = SimpleTimeMark.now() + outgoing.estimateDelay()

    @HandleEvent(eventType = PreModInitializationEvent::class)
    fun onPreModInitializationEvent() {
        ClientSendMessageEvents.CHAT.register { outgoing.recordSent() }
        ClientSendMessageEvents.COMMAND.register { outgoing.recordSent() }
    }

    @HandleEvent(eventType = ClientDisconnectEvent::class)
    fun onTickEvent() { dispatchNext(); actions.cleanup() }

    @HandleEvent(eventType = ClientDisconnectEvent::class)
    fun onClientDisconnectEvent() {
        outgoing.clear()
        actions.clear()
        messages.clear()
        deleteNext = null
    }

    fun chatAndOpenConfig(message: String, property: KProperty0<*>) =
        clickableChat(message, { openSetting(property) }, "§eClick to find this setting!")

    fun notifyOrDisable(message: String, option: KProperty0<*>, oneTimeClick: Boolean = false, messageId: Int? = null) =
        clickableChat(message, { openSetting(option) }, "§eClick to find and disable this setting!", oneTimeClick = oneTimeClick, replaceSameMessage = true, messageId = messageId)

    fun clickToActionOrDisable(message: String, option: KProperty0<*>, actionName: String, action: () -> Unit, oneTimeClick: Boolean = false) =
        clickableChat(message, {
            if (Minecraft.getInstance().hasShiftDown()) openSetting(option) else action()
        }, "§eClick to $actionName!\n§eShift-click to find this setting.", oneTimeClick = oneTimeClick, replaceSameMessage = true)

    private fun openSetting(property: KProperty0<*>) {
        val manager = EurybiumMod.configManager
        property.javaField?.let { manager.getEditorInstance().getOptionFromField(it) }?.let { manager.getEditorInstance().goToOption(it) }
        manager.openConfigGui()
    }

    fun consoleLog(text: String) = EurybiumMod.logger.info("[Chat] {}", text)
}
