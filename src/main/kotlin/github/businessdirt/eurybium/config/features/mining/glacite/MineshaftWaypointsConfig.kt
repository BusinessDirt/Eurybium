package github.businessdirt.eurybium.config.features.mining.glacite

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MineshaftWaypointsConfig {

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(name = "Enabled", desc = "Enable Glacite Mineshaft waypoints.")
    var enabled: Boolean = false

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(name = "Fancy Waypoints", desc = "Use the Minecraft glow effect to highlight waypoints §e(WARNING: can cause lag)")
    var renderFancy: Boolean = false
}