package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import github.businessdirt.eurybium.data.model.MiningNodeMaterial
import github.businessdirt.eurybium.data.model.MiningNodeRegion
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

/** Live mining-node expansion with independent region switches and resource exclusions. */
class WaypointNodeGlowConfig {
    @ConfigEditorBoolean
    @ConfigOption(name = "Nearby Mining Nodes", desc = "Expand Glow waypoints to nearby connected mining resources in the regions enabled below.")
    @Expose var expandNodes: Boolean = false

    @ConfigEditorSlider(minValue = 0f, maxValue = 32f, minStep = 0.5f)
    @ConfigOption(name = "Node Match Range", desc = "Maximum distance from a waypoint to a node block. Larger ranges take longer to scan. No match keeps single-block glow.")
    @Expose var matchRange: Float = 3f

    @Accordion
    @ConfigOption(name = "Mineshafts", desc = "Expansion and excluded resources inside mineshafts.")
    @Expose var mineshafts: RegionNodeGlowConfig = RegionNodeGlowConfig()

    @Accordion
    @ConfigOption(name = "Glacite Tunnels", desc = "Expansion and excluded resources in the tunnels and Dwarven Base Camp.")
    @Expose var glaciteTunnels: RegionNodeGlowConfig = RegionNodeGlowConfig()

    @Accordion
    @ConfigOption(name = "Crystal Hollows", desc = "Expansion and excluded resources in the Crystal Hollows.")
    @Expose var crystalHollows: RegionNodeGlowConfig = RegionNodeGlowConfig()

    /** Snapshots exclusions so changing the UI immediately invalidates any active scan policy. */
    fun policy(region: MiningNodeRegion?): NodeExpansionPolicy? {
        if (!expandNodes || region == null) return null
        val settings = when (region) {
            MiningNodeRegion.MINESHAFT -> mineshafts
            MiningNodeRegion.GLACITE_TUNNELS -> glaciteTunnels
            MiningNodeRegion.CRYSTAL_HOLLOWS -> crystalHollows
        }

        if (!settings.enabled) return null
        return NodeExpansionPolicy(region, MiningNodeMaterial.entries.toSet() - settings.excludedMaterials.toSet())
    }
}

/** An empty exclusion list allows every supported resource; each region owns its own list. */
class RegionNodeGlowConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Enabled", desc = "Expand waypoint blocks to mining nodes in this region.")
    @Expose var enabled: Boolean = true

    @ConfigEditorDraggableList
    @ConfigOption(name = "Excluded Materials", desc = "Add resources that should never be chosen for node expansion. Exclusions also apply to waypoints with an explicit material preference. List order does not matter.")
    @Expose var excludedMaterials: MutableList<MiningNodeMaterial> = mutableListOf()
}

/** Immutable context used by matching and cache invalidation, independent of mutable config lists. */
data class NodeExpansionPolicy(val region: MiningNodeRegion, val allowedMaterials: Set<MiningNodeMaterial>)
