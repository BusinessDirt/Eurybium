package github.businessdirt.eurybium.api.minecraft

import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.copyTree
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.legacyString
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.TabListFooterUpdateEvent
import github.businessdirt.eurybium.events.minecraft.TabListUpdateEvent
import github.businessdirt.eurybium.events.minecraft.TickEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.minecraft.mixin.PlayerTabOverlayAccessor
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

/** Client-thread snapshot of the displayed tab list, refreshed after Minecraft handles packets. */
@EurybiumModule
object TabListAPI {

    /** Listed entries in Minecraft's display order, including blank lines (at most 80 entries). */
    var lines: List<Component> = emptyList()
        private set

    var header: Component? = null
        private set

    var footer: Component? = null
        private set

    val linesFormatted: List<String> get() = lines.map { it.legacyString() }
    val linesPlain: List<String> get() = linesFormatted.map { it.removeColor() }

    @HandleEvent(events = [TickEvent::class], priority = HandleEvent.HIGHEST)
    private fun onTick() = refresh()

    @HandleEvent(events = [WorldChangeEvent::class, ClientDisconnectEvent::class], priority = HandleEvent.HIGHEST)
    private fun onClear() = update(emptyList(), null, null)

    fun refresh() {
        val minecraft = Minecraft.getInstance()
        if (minecraft.player == null || minecraft.level == null) {
            onClear()
            return
        }

        val overlay = minecraft.gui.tabList
        val access = overlay as PlayerTabOverlayAccessor
        update(
            access.eurybiumPlayerInfos().map(overlay::getNameForDisplay),
            access.eurybiumHeader,
            access.eurybiumFooter,
        )
    }

    /** Publish all fields before notifying listeners; footer changes do not depend on body changes. */
    internal fun update(newLines: List<Component>, newHeader: Component?, newFooter: Component?) {
        val previousLines = lines
        val previousFooter = footer
        val bodyChanged = newLines != previousLines
        val footerChanged = newFooter != previousFooter

        // Copy incoming component trees so later HUD mutations cannot silently change the cache.
        if (bodyChanged) lines = newLines.map { it.copyTree() }
        if (newHeader != header) header = newHeader?.copyTree()
        if (footerChanged) footer = newFooter?.copyTree()

        if (bodyChanged) TabListUpdateEvent(lines, previousLines).post()
        if (footerChanged) TabListFooterUpdateEvent(footer, previousFooter).post()
    }

    @HandleEvent
    private fun onCommandRegistration(event: CommandRegistrationEvent) {
        for (components in listOf(false, true)) {
            event.register(if (components) "eybcopytablistcomponent" else "eybcopytablist") {
                category = CommandCategory.DEVELOPER_DEBUG
                description = "Copy tab list ${if (components) "components" else "formatted text"} to the clipboard."
                simpleCallback {
                    refresh()
                    Minecraft.getInstance().keyboardHandler.clipboard = copyText(components)
                    ChatAPI.chat("Tab list copied to clipboard.")
                }
            }
        }
    }

    internal fun copyText(components: Boolean): String {
        fun Component?.text(): String = when {
            this == null -> ""
            components -> toString()
            else -> legacyString()
        }

        val body = lines.joinToString("\n") { if (it.string.isEmpty()) " " else it.text() }
        return "Header:\n\n${header.text()}\n\nBody:\n\n$body\n\nFooter:\n\n${footer.text()}"
    }
}
