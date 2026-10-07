package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/** Notification preferences independent of mineshaft route loading. */
class MiningNotificationsConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Mining Ability Ready", desc = "Notify once when your mining ability cooldown finishes.")
    @Expose var miningAbilityNotification: Boolean = false
}
