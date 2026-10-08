package github.businessdirt.eurybium.features.mining

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.text.LegacyFormatting.legacyString
import github.businessdirt.eurybium.api.repo.RepoPattern
import github.businessdirt.eurybium.events.minecraft.GameMessageReceivedEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

/** Shows an ability-ready title and sound when Hypixel announces the end of its cooldown. */
@EurybiumModule
object MiningNotifications {

    private val config get() = EurybiumMod.config.mining.notifications
    private val abilityReadyPattern = RepoPattern(
        "mining.ability.ready",
        """(?:§r)*§6(?<ability>[^§]+) (?:§r)*§ais now available!(?:§r)*""",
    )

    @HandleEvent(onlyOnSkyBlock = true)
    private fun onGameMessageReceivedEvent(event: GameMessageReceivedEvent) {
        val ability = readyAbility(event, config.miningAbilityNotification) ?: return
        val minecraft = getMinecraft()
        val player = minecraft.player ?: return

        minecraft.gui.setTimes(5, 40, 10)
        minecraft.gui.setSubtitle(Component.literal("is now available!").withStyle(ChatFormatting.GREEN))
        minecraft.gui.setTitle(Component.literal(ability).withStyle(ChatFormatting.GOLD))
        player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f)
    }

    /** Matches server chat only; action-bar messages and disabled notifications do no pattern work. */
    internal fun readyAbility(
        event: GameMessageReceivedEvent,
        enabled: Boolean,
        pattern: Regex? = abilityReadyPattern.resolve(),
    ): String? {
        if (!enabled || event.overlay || pattern == null) return null
        return pattern.matchEntire(event.message.legacyString())
            ?.groups?.get("ability")?.value?.trim()?.takeIf { it.isNotEmpty() }
    }
}
