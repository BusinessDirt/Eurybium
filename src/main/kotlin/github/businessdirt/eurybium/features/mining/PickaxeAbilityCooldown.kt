package github.businessdirt.eurybium.features.mining

import gg.essential.universal.UMinecraft.getMinecraft
import gg.essential.universal.USound
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.events.minecraft.AllowGameMessageEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

object PickaxeAbilityCooldown {

    private val abilityPattern = Pattern.compile("(?<ability>[a-zA-Z]) is now available!")

    @HandleEvent
    fun onAllowGameMessageEvent(event: AllowGameMessageEvent) {
        if (!EurybiumMod.config.mining.pickaxeAbilityNotification) return

        val siblings = event.message.siblings
        if (siblings.size >= 3 &&
            siblings[2].string.contains("is now available!")) {
            getMinecraft().inGameHud.setTitle(Text.of(Formatting.GOLD.toString() + siblings[1].string.trim()))
            USound.playPlingSound()
        }
    }
}