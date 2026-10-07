package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.config.features.mining.glacitemineshaft.MineshaftConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/** Mining-specific settings; general ordered-route rendering remains in Ordered Waypoints. */
class MiningConfig {

    @Accordion
    @ConfigOption(name = "Waypoint Node Glow", desc = "Expand Glow waypoints to nearby mining nodes using repository data.")
    @Expose var waypointNodes: WaypointNodeGlowConfig = WaypointNodeGlowConfig()

    @Accordion
    @ConfigOption(name = "Notifications", desc = "Mining ability notifications.")
    @Expose var notifications: MiningNotificationsConfig = MiningNotificationsConfig()

    @Category(name = "Glacite Mineshaft", desc = "Mineshaft announcements, automatic routes, and corpse notifications.")
    @Expose var glaciteMineshaft: MineshaftConfig = MineshaftConfig()
}
