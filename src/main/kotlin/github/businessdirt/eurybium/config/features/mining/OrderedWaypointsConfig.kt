package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class OrderedWaypointsConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Enable Ordered Waypoints", desc = "Enable Ordered Waypoints")
    @Expose var enabled: Boolean = true

    @ConfigEditorDropdown
    @ConfigOption(name = "Render Mode", desc = "What render mode to use §e(WARNING: Glow can cause lag)")
    @Expose var renderMode: RenderMode = RenderMode.OUTLINE

    @ConfigEditorColour
    @ConfigOption(name = "Previous Color", desc = "Color of the previous waypoint")
    @Expose var previousWaypointColor: ChromaColour = ChromaColour.fromRGB(255, 100, 100, 0, 153)

    @ConfigEditorColour
    @ConfigOption(name = "Current Color", desc = "Color of the current waypoint")
    @Expose var currentWaypointColor: ChromaColour = ChromaColour.fromRGB(255, 255, 255, 0, 153)

    @ConfigEditorColour
    @ConfigOption(name = "Next Color", desc = "Color of the next waypoint")
    @Expose var nextWaypointColor: ChromaColour = ChromaColour.fromRGB(100, 255, 100, 0, 153)

    @ConfigOption(name = "Block Outline Thickness", desc = "Thickness of the block outline.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f)
    @Expose var blockOutlineThickness: Float = 1f

    @ConfigOption(name = "Waypoint Range", desc = "How close you have to be for it to go to the next waypoint.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 0.1f)
    @Expose var waypointRange: Float = 3f

    @ConfigOption(name = "Enable trace line", desc = "Enables the trace line.")
    @ConfigEditorBoolean
    @Expose var traceLine: Boolean = true

    @ConfigOption(name = "Trace Line Color", desc = "Color of the trace line.")
    @ConfigEditorColour
    @Expose var traceLineColor: ChromaColour = ChromaColour.fromRGB(85, 255, 85, 0, 255)

    @ConfigOption(name = "Trace Line Thickness", desc = "Thickness of the trace line.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f)
    @Expose var traceLineThickness: Float = 1.0f

    enum class RenderMode {
        OUTLINE, FILL, GLOW
    }
}