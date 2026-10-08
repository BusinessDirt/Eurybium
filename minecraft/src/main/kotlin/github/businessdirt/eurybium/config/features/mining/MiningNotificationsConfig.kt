package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/** Notification preferences independent of mineshaft route loading. */
class MiningNotificationsConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Mining Ability Ready", desc = "Show a title and play a sound when your mining ability becomes available.")
    @Expose var miningAbilityNotification: Boolean = false
}
