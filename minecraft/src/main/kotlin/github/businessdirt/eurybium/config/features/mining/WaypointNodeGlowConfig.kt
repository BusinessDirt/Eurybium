package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/** Optional mineshaft gemstone expansion for ordered waypoints in Glow mode. No match keeps the single-block glow. */
class WaypointNodeGlowConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Mineshaft Gemstones", desc = "Glow the entire gemstone node near a waypoint in a mineshaft.")
    @Expose var mineshaftGemstones: Boolean = false

    @ConfigEditorSlider(minValue = 0f, maxValue = 32f, minStep = 0.5f)
    @ConfigOption(name = "Node Match Range", desc = "Maximum distance in blocks from a waypoint to the nearest block of a node. Zero requires the waypoint to be on a node block. The trace line targets the matched node's center.")
    @Expose var matchRange: Float = 3f
}
