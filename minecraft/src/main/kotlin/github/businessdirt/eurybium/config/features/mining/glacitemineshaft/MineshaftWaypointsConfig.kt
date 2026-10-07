package github.businessdirt.eurybium.config.features.mining.glacitemineshaft

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.data.model.waypoints.MiningRouteIds
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/** Automatic route choices. Spawning routes and shaft-entry routes can be enabled independently. */
class MineshaftWaypointsConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Auto Load Mineshaft Route", desc = "Load the repository route for the detected shaft, such as eurybium:JASP1. A missing route leaves your current route unchanged.")
    @Expose var autoLoadMineshaft: Boolean = false

    @ConfigEditorDropdown
    @ConfigOption(name = "Spawning Route", desc = "Load this route on entering Dwarven Base Camp. None disables automatic spawning routes; Custom uses the name below.")
    @Expose var spawningRoute: SpawningRoute = SpawningRoute.NONE

    @ConfigEditorText
    @ConfigOption(name = "Custom Spawning Route", desc = "Name of your saved route, used only when Spawning Route is Custom. Existing custom spawning routes are preserved here.")
    @Expose var customSpawningRoute: String = ""

    /** Built-in route IDs are shared with the repository; Custom preserves previously saved user routes. */
    enum class SpawningRoute(val routeId: String?, private val label: String) {
        NONE(null, "None"),
        MITHRIL(MiningRouteIds.SHAFT_SPAWN_MITHRIL, "Mithril"),
        TUNGSTEN(MiningRouteIds.SHAFT_SPAWN_TUNGSTEN, "Tungsten"),
        GEMSTONES(MiningRouteIds.SHAFT_SPAWN_GEMSTONES, "GEMSTONES"),
        CUSTOM(null, "Custom"),
        ;

        override fun toString(): String = label
    }
}
