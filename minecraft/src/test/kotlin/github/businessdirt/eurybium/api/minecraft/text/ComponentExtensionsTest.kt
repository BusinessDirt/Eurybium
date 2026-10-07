package github.businessdirt.eurybium.api.minecraft.text

import github.businessdirt.eurybium.api.minecraft.chat.Chat
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.asComponent
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.asLiteralComponent
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.command
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.copyToClipboard
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.hover
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.hoverTextLines
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.multiline
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.suggestCommand
import github.businessdirt.eurybium.api.minecraft.text.ComponentExtensions.url
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.Optional
import kotlin.test.*

class ComponentExtensionsTest {
    private fun segments(component: Component): List<Pair<String, Style>> = buildList {
        component.visit({ style, text ->
            if (text.isNotEmpty()) add(text to style)
            Optional.empty<Unit>()
        }, Style.EMPTY)
    }

    @Test
    fun `legacy colors reset decorations and reset formatting overrides parent style`() {
        val text = "§lbold§ared§rplain".asComponent { withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC) }
        val parts = segments(text)
        assertEquals(listOf("bold", "red", "plain"), parts.map { it.first })
        assertTrue(parts[0].second.isBold)
        assertTrue(parts[0].second.isItalic)
        assertEquals(ChatFormatting.BLUE.color, parts[0].second.color?.value)
        assertFalse(parts[1].second.isBold)
        assertFalse(parts[1].second.isItalic)
        assertEquals(ChatFormatting.GREEN.color, parts[1].second.color?.value)
        assertFalse(parts[2].second.isBold)
        assertEquals(ChatFormatting.WHITE.color, parts[2].second.color?.value)
    }

    @Test
    fun `invalid formatting is retained and literal conversion does not parse codes`() {
        assertEquals("unknown§z and trailing§", "unknown§z and trailing§".asComponent().string)
        assertEquals("§aunchanged", "§aunchanged".asLiteralComponent().string)
        assertEquals("upper", "§Aupper".asComponent().string)
    }

    @Test
    fun `hover command suggestion URL and clipboard actions use native events`() {
        val component = "Click".asComponent()
        component.hover = "§eInformation".asComponent()
        assertEquals("Information", component.hover?.string)
        component.command = "eybo load eurybium:JASP1"
        assertEquals("/eybo load eurybium:JASP1", component.command)
        component.suggestCommand = "/hello"
        assertEquals("/hello", component.suggestCommand)
        component.url = "https://example.org/help"
        assertEquals("https://example.org/help", component.url)
        component.copyToClipboard("copied")
        assertIs<ClickEvent.CopyToClipboard>(component.style.clickEvent)
        assertFailsWith<IllegalArgumentException> { component.url = "file:///tmp/example" }
        assertFailsWith<IllegalArgumentException> { component.url = "relative-path" }
    }

    @Test
    fun `multiline hovers are gathered in component display order`() {
        val text = "root".asComponent { hover = multiline(listOf("§afirst", "second")) }
        text.append("child".asComponent { hover = "third".asComponent() })
        assertEquals(listOf("first", "second", "third"), text.hoverTextLines().map { it.removeColor() })
    }

    @Test
    fun `formatting a chat message copies siblings and does not color the message with its prefix`() {
        val child = Component.literal("child")
        val original = Component.literal("body").append(child)
        val formatted = Chat.formatted(original, true, ChatFormatting.RED.color)
        child.append("changed")
        original.append("more")
        assertEquals("[Eurybium] bodychild", formatted.string)
        val parts = segments(formatted)
        assertEquals(ChatFormatting.RED.color, parts.first().second.color?.value)
        assertNull(parts[1].second.color)
        assertEquals(ChatFormatting.GREEN.color, Chat.legacyColor("§a"))
        assertFailsWith<IllegalArgumentException> { Chat.legacyColor("") }
        assertFailsWith<IllegalArgumentException> { Chat.legacyColor("§l") }
    }
}
