package github.businessdirt.eurybium.config.features.waypoints

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.*

/** Settings for ordered routes in any world, independent of mining features. */
// ID-based accordions keep the existing serialized fields flat instead of nesting UI sections.
@Suppress("DEPRECATION")
class OrderedWaypointsConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Enable Ordered Waypoints", desc = "Render the active route and advance to nearby waypoints.")
    @Expose var enabled: Boolean = true

    @ConfigOption(name = "Rendering", desc = "Choose how waypoint blocks appear.")
    @ConfigEditorAccordion(id = 0)
    var rendering: Boolean = false

    @ConfigAccordionId(id = 0)
    @ConfigEditorDropdown
    @ConfigOption(name = "Render Mode", desc = "Outline and Fill are visible through terrain. Glow highlights the block model and can cost more performance.")
    @Expose var renderMode: RenderMode = RenderMode.OUTLINE

    @ConfigAccordionId(id = 0)
    @ConfigEditorColour
    @ConfigOption(name = "Previous Color", desc = "Color of the previous waypoint")
    @Expose var previousWaypointColor: ChromaColour = ChromaColour.fromRGB(255, 100, 100, 0, 153)

    @ConfigAccordionId(id = 0)
    @ConfigEditorColour
    @ConfigOption(name = "Current Color", desc = "Color of the current waypoint")
    @Expose var currentWaypointColor: ChromaColour = ChromaColour.fromRGB(255, 255, 255, 0, 153)

    @ConfigAccordionId(id = 0)
    @ConfigEditorColour
    @ConfigOption(name = "Next Color", desc = "Color of the next waypoint")
    @Expose var nextWaypointColor: ChromaColour = ChromaColour.fromRGB(100, 255, 100, 0, 153)

    @ConfigAccordionId(id = 0)
    @ConfigOption(name = "Outline Thickness", desc = "Thickness of waypoint edges in Outline mode.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 20f, minStep = 1f)
    @Expose var blockOutlineThickness: Float = 5f

    @ConfigOption(name = "Navigation", desc = "Control when the route advances.")
    @ConfigEditorAccordion(id = 1)
    var navigation: Boolean = false

    @ConfigAccordionId(id = 1)
    @ConfigOption(name = "Waypoint Range", desc = "Advance when the next waypoint is within this many blocks and closer than the current waypoint.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 0.1f)
    @Expose var waypointRange: Float = 3f

    @ConfigOption(name = "Trace Line", desc = "Draw a line toward the next waypoint.")
    @ConfigEditorAccordion(id = 2)
    var traceLineSettings: Boolean = false

    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Enable Trace Line", desc = "Show a line from your view toward the next waypoint.")
    @ConfigEditorBoolean
    @Expose var traceLine: Boolean = true

    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Trace Line Color", desc = "Color of the trace line.")
    @ConfigEditorColour
    @Expose var traceLineColor: ChromaColour = ChromaColour.fromRGB(85, 255, 85, 0, 255)

    @ConfigAccordionId(id = 2)
    @ConfigOption(name = "Trace Line Thickness", desc = "Thickness of the trace line.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 10f, minStep = 1f)
    @Expose var traceLineThickness: Float = 2.0f

    enum class RenderMode {
        OUTLINE, FILL, GLOW
    }
}
