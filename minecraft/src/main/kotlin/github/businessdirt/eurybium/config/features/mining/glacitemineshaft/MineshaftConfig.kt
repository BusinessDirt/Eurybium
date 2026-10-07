package github.businessdirt.eurybium.config.features.mining.glacitemineshaft

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/** Mineshaft preferences. Internal location detection must not depend on announcement preferences. */
class MineshaftConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Mineshaft Type Notification", desc = "Display the detected mineshaft type when entering a shaft.")
    @Expose var detectMineshaft: Boolean = false

    @Accordion
    @ConfigOption(name = "Automatic Waypoints", desc = "Load repository routes when entering a shaft or the spawning area.")
    @Expose var waypoints: MineshaftWaypointsConfig = MineshaftWaypointsConfig()

    @Accordion
    @ConfigOption(name = "Corpse Notifications", desc = "Notify when the shaft reaches the configured corpse threshold.")
    @Expose var corpseNotifications: CorpseNotificationsConfig = CorpseNotificationsConfig()
}
