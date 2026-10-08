package github.businessdirt.eurybium.features.mining

import github.businessdirt.eurybium.events.minecraft.GameMessageReceivedEvent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MiningNotificationsTest {

    private fun event(text: String, overlay: Boolean = false) = GameMessageReceivedEvent(Component.literal(text), overlay)

    @Test
    fun `server announcement captures different ability names`() {
        for (ability in listOf("Mining Speed Boost", "Pickobulus", "Gemstone Infusion")) {
            assertEquals(ability, MiningNotifications.readyAbility(event("§r§6$ability §r§ais now available!§r"), true))
        }
    }

    @Test
    fun `styled components match without a trailing reset code`() {
        val message = Component.literal("Mining Speed Boost ").withStyle(ChatFormatting.GOLD)
            .append(Component.literal("is now available!").withStyle(ChatFormatting.GREEN))
        assertEquals("Mining Speed Boost", MiningNotifications.readyAbility(GameMessageReceivedEvent(message, false), true))
    }

    @Test
    fun `disabled notifications action bars and absent patterns are ignored`() {
        val message = "§r§6Mining Speed Boost §r§ais now available!§r"
        assertNull(MiningNotifications.readyAbility(event(message), false))
        assertNull(MiningNotifications.readyAbility(event(message, overlay = true), true))
        assertNull(MiningNotifications.readyAbility(event(message), true, null))
    }

    @Test
    fun `player messages unrelated messages and blank ability names cannot notify`() {
        for (text in listOf(
            "§r§6 §r§ais now available!§r",
            "§r§6Mining Speed Boost §r§cis now available!§r",
            "§r§6Mining Speed Boost §r§ahas expired!§r",
            "Player: §r§6Mining Speed Boost §r§ais now available!§r",
            "Mining Speed Boost is now available!",
        )) {
            assertNull(MiningNotifications.readyAbility(event(text), true), text)
        }
    }
}
