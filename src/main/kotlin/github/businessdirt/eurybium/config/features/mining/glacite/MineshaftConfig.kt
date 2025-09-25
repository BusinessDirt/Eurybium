package github.businessdirt.eurybium.config.features.mining.glacite

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MineshaftConfig {

    @Expose
    @Accordion
    @ConfigOption(name = "Mineshaft Waypoints", desc = "General waypoints inside the Mineshaft.")
    var mineshaftWaypoints: MineshaftWaypointsConfig = MineshaftWaypointsConfig()

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(
        name = "Mineshaft Detection",
        desc = "Detects when you enter a mineshaft and displays the type of mineshaft you entered."
    )
    var detectMineshaft: Boolean = false

    var gemstonePriceThreshold: Float = 13.0f
    var mineshaftCorpseMultiplier: Float = 1.1f
}
