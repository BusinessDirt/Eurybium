package github.businessdirt.eurybium.api.minecraft.text

import github.businessdirt.eurybium.api.minecraft.chat.Chat
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.legacyString
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.chat.GuiMessage
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import java.net.URI
import kotlin.time.Duration.Companion.milliseconds

/** Styled text construction; chat delivery and action lifetime belong to [Chat]. */
object ComponentExtensions {
    private val resetStyle = Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false).withItalic(false)
        .withUnderlined(false).withStrikethrough(false).withObfuscated(false)

    /** Parses section-sign colors/formatting into native styles, retaining unrecognized codes as text. */
    fun String.asComponent(init: MutableComponent.() -> Unit = {}): MutableComponent {
        val result = Component.empty()
        val text = StringBuilder()
        var style = Style.EMPTY

        fun flush() {
            if (text.isNotEmpty()) {
                result.append(Component.literal(text.toString()).setStyle(style))
                text.setLength(0)
            }
        }

        var index = 0

        while (index < length) {
            val formatting = if (this[index] == '§' && index + 1 < length) ChatFormatting.getByCode(this[index + 1]) else null

            if (formatting == null) {
                text.append(this[index++])
            } else {
                flush()

                // Explicit false flags prevent reset/color segments inheriting decoration from a parent.
                style = when {
                    formatting == ChatFormatting.RESET -> resetStyle
                    formatting.isColor -> resetStyle.withColor(formatting)
                    else -> style.applyLegacyFormat(formatting)
                }

                index += 2
            }
        }

        flush()

        return result.also(init)
    }

    /** Creates text without interpreting section-sign formatting. */
    fun String.asLiteralComponent(init: MutableComponent.() -> Unit = {}): MutableComponent = Component.literal(this).also(init)

    fun componentBuilder(init: MutableComponent.() -> Unit): MutableComponent = Component.empty().also(init)

    /** Copies component styles and sibling trees so subsequent caller edits cannot change sent text. */
    fun Component.copyTree(): MutableComponent = MutableComponent.create(contents).setStyle(style).also { copy ->
        siblings.forEach { copy.append(it.copyTree()) }
    }

    /** Appends formatted text with an optional initializer for just that segment. */
    fun MutableComponent.append(text: String, init: MutableComponent.() -> Unit): MutableComponent = append(text.asComponent(init))

    fun multiline(lines: Iterable<String>): MutableComponent = componentBuilder {
        lines.forEachIndexed { index, line ->
            if (index > 0) append("\n")
            append(line.asComponent())
        }
    }

    var MutableComponent.hover: Component?
        get() = (style.hoverEvent as? HoverEvent.ShowText)?.value()
        set(value) { style = style.withHoverEvent(value?.let { HoverEvent.ShowText(it.copyTree()) }) }

    var MutableComponent.command: String?
        get() = (style.clickEvent as? ClickEvent.RunCommand)?.command()
        set(value) { style = style.withClickEvent(value?.let { ClickEvent.RunCommand("/" + it.removePrefix("/")) }) }

    var MutableComponent.suggestCommand: String?
        get() = (style.clickEvent as? ClickEvent.SuggestCommand)?.command()
        set(value) { style = style.withClickEvent(value?.let(ClickEvent::SuggestCommand)) }

    var MutableComponent.url: String?
        get() = (style.clickEvent as? ClickEvent.OpenUrl)?.uri()?.toString()
        set(value) { style = style.withClickEvent(value?.let { ClickEvent.OpenUrl(webUri(it)) }) }

    /** Validates URLs before both native link clicks and optional immediate browser opening. */
    internal fun webUri(value: String): URI = URI(value).also {
        require(it.scheme?.lowercase() in setOf("http", "https") && !it.host.isNullOrBlank()) { "Expected an absolute HTTP(S) URL" }
    }

    /** Attaches a local action; expiration and one-time claims are enforced by Chat's action registry. */
    fun MutableComponent.onClick(
        expireAt: SimpleTimeMark = SimpleTimeMark.farFuture(),
        oneTimeClick: Boolean = false,
        onClick: () -> Unit,
    ): MutableComponent = apply { style = style.withClickEvent(Chat.createClickAction(expireAt, oneTimeClick, onClick)) }

    fun MutableComponent.copyToClipboard(text: String): MutableComponent = apply {
        style = style.withClickEvent(ClickEvent.CopyToClipboard(text))
    }

    /** Returns explicit text hovers on the component and its siblings in display order. */
    fun Component.hoverTextLines(): List<String> = buildList {
        val remaining = ArrayDeque<Component>()
        remaining.add(this@hoverTextLines)

        while (remaining.isNotEmpty()) {
            val component = remaining.removeLast()
            (component.style.hoverEvent as? HoverEvent.ShowText)?.value()?.legacyString()?.split('\n')?.let(::addAll)
            component.siblings.asReversed().forEach(remaining::addLast)
        }
    }

    /** Sends unprefixed local chat; safe to call from a background task. */
    fun Component.addToChat() = Chat.chat(this, prefix = false)

    /** Sends an unprefixed local message with a replaceable ID. */
    fun Component.send(messageId: Int? = null) = Chat.chat(this, prefix = false, messageId = messageId)

    /** True for a component currently tracked as an Eurybium-created chat entry. */
    val Component.eurybiumCreated: Boolean get() = Chat.isOwnMessage(this)

    val GuiMessage.chatMessage: String get() = content().legacyString()

    /** Minecraft HUD ticks use 50 ms; clamp a cleared/reset HUD clock to zero. */
    fun GuiMessage.passedSinceSent() = ((Minecraft.getInstance().gui.guiTicks - addedTime()).coerceAtLeast(0).toLong() * 50).milliseconds
}
