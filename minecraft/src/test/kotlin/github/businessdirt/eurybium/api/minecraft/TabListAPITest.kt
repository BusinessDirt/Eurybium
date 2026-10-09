package github.businessdirt.eurybium.api.minecraft

import github.businessdirt.eurybium.api.events.EventBusTestFixture
import github.businessdirt.eurybium.api.events.EurybiumEventBus
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.removeColor
import github.businessdirt.eurybium.events.minecraft.TabListFooterUpdateEvent
import github.businessdirt.eurybium.events.minecraft.TabListUpdateEvent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TabListAPITest : EventBusTestFixture() {
    @AfterTest
    fun clearSnapshot() = TabListAPI.update(emptyList(), null, null)

    @Test
    fun `footer updates and clearing are delivered without a body update`() {
        TabListAPI.update(emptyList(), null, null)
        val events = Changes().also(EurybiumEventBus::register)
        val body = listOf(Component.literal("Player"))
        TabListAPI.update(body, Component.literal("Header"), Component.literal("First footer"))
        TabListAPI.update(body, Component.literal("New header"), Component.literal("Second\nfooter"))
        TabListAPI.update(body, Component.literal("New header"), null)
        TabListAPI.update(body, Component.literal("New header"), null)

        assertEquals(1, events.body.size)
        assertEquals(3, events.footer.size)
        assertEquals(listOf("Second", "footer"), events.footer[1].lines.map { it.removeColor() })
        assertEquals("Second\nfooter", events.footer.last().oldFooter?.string)
        assertNull(events.footer.last().newFooter)
        assertEquals("New header", TabListAPI.header?.string)
        assertNull(TabListAPI.footer)
    }

    @Test
    fun `single entry blank lines and style changes are retained`() {
        TabListAPI.update(emptyList(), null, null)
        val events = Changes().also(EurybiumEventBus::register)
        TabListAPI.update(listOf(Component.literal("Only player")), null, null)
        assertEquals(listOf("Only player"), TabListAPI.linesPlain)
        TabListAPI.update(listOf(Component.literal("Only player").withStyle(ChatFormatting.GREEN), Component.empty()), null, null)
        assertEquals(listOf("Only player", ""), TabListAPI.linesPlain)
        assertTrue(TabListAPI.linesFormatted.first().contains("§a"))
        TabListAPI.update(listOf(Component.literal("Only player").withStyle(ChatFormatting.RED), Component.empty()), null, null)
        assertEquals(3, events.body.size)
        TabListAPI.update(emptyList(), null, null)
        assertEquals(4, events.body.size)
        assertEquals(2, events.body.last().oldLines.size)
        assertTrue(events.body.last().newLines.isEmpty())
    }

    @Test
    fun `HUD component mutation cannot change cached text or previous events`() {
        TabListAPI.update(emptyList(), null, null)
        val events = Changes().also(EurybiumEventBus::register)
        val name = Component.literal("Player")
        val footer = Component.literal("Footer")
        TabListAPI.update(listOf(name), null, footer)
        name.append(" changed")
        footer.append(" changed")
        assertEquals(listOf("Player"), TabListAPI.linesPlain)
        assertEquals("Footer", TabListAPI.footer?.string)
        assertEquals("Player", events.body.single().newLines.single().string)
        TabListAPI.update(listOf(name), null, footer)
        assertEquals(2, events.body.size)
        assertEquals("Player", events.body.last().oldLines.single().string)
        assertEquals("Footer", events.footer.last().oldFooter?.string)
        assertTrue(TabListAPI.copyText(false).removeColor().contains("Player changed"))
    }

    private class Changes {
        val body = mutableListOf<TabListUpdateEvent>()
        val footer = mutableListOf<TabListFooterUpdateEvent>()

        @HandleEvent
        private fun onBody(event: TabListUpdateEvent) { body.add(event) }

        @HandleEvent
        private fun onFooter(event: TabListFooterUpdateEvent) { footer.add(event) }
    }
}
