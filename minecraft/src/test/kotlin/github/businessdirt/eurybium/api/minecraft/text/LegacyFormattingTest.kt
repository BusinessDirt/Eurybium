package github.businessdirt.eurybium.api.minecraft.text

import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.legacyString
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.stripLeadingAndTrailingColorResetFormatting
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import org.junit.jupiter.api.Test
import kotlin.test.*

class LegacyFormattingTest {

    @Test
    fun `formatting survives color stripping but colors reset active styles`() {
        assertEquals("§lbold§rplain", "§lbold§aplain".removeColor(keepFormatting = true))
        assertEquals("§Lbold§rplain", "§Lbold§Aplain".removeColor(keepFormatting = true))
        assertEquals("boldplain", "§lbold§aplain".removeColor())
        assertEquals("text", "text§".removeColor())
        assertEquals("text", "§ztext".removeColor())
    }

    @Test
    fun `mutable character sequences are converted only once`() {
        var conversions = 0
        val text = "§aone§btwo§cthree"
        val sequence = object : CharSequence by text {
            override fun toString(): String { conversions++; return text }
        }
        assertEquals("onetwothree", sequence.removeColor())
        assertEquals(1, conversions)
    }

    @Test
    fun `edge reset stripping preserves internal formatting`() {
        assertEquals("one§rtwo", "§r§rone§rtwo§r§r".stripLeadingAndTrailingColorResetFormatting())
        assertEquals("", "§r§r".stripLeadingAndTrailingColorResetFormatting())
        assertEquals("§Rtext§R", "§Rtext§R".stripLeadingAndTrailingColorResetFormatting())
    }

    @Test
    fun `component conversion preserves inherited named color and style`() {
        val text = Component.literal("one").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            .append(Component.literal("two"))
        assertEquals("§r§c§lone§r§c§ltwo", text.legacyString())
        assertEquals("onetwo", text.legacyString().removeColor())
    }
}
