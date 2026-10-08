package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.data.model.MiningNodeMaterial
import io.github.notenoughupdates.moulconfig.annotations.*

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
    @Expose var glaciteTunnels: RegionNodeGlowConfig = RegionNodeGlowConfig(enabled = false)

    @Accordion
    @ConfigOption(name = "Crystal Hollows", desc = "Expansion and excluded resources in the Crystal Hollows.")
    @Expose var crystalHollows: RegionNodeGlowConfig = RegionNodeGlowConfig()

    /** Snapshots exclusions so changing the UI immediately invalidates any active scan policy. */
    fun policy(
        inMineshaft: Boolean = false,
        inGlaciteTunnels: Boolean = false,
        inCrystalHollows: Boolean = false,
    ): NodeExpansionPolicy? {
        if (!expandNodes) return null
        val settings = when {
            inMineshaft -> mineshafts
            inGlaciteTunnels -> glaciteTunnels
            inCrystalHollows -> crystalHollows
            else -> return null
        }

        if (!settings.enabled) return null
        return NodeExpansionPolicy(
            dwarvenMaterials = inMineshaft || inGlaciteTunnels,
            inCrystalHollows = !inMineshaft && !inGlaciteTunnels && inCrystalHollows,
            allowedMaterials = MiningNodeMaterial.entries.toSet() - settings.excludedMaterials.toSet(),
        )
    }
}

/** An empty exclusion list allows every supported resource; each region owns its own list. */
class RegionNodeGlowConfig(
    @ConfigEditorBoolean @ConfigOption(
        name = "Enabled",
        desc = "Expand waypoint blocks to mining nodes in this region."
    ) @Expose var enabled: Boolean = true,

    @ConfigEditorDraggableList
    @ConfigOption(name = "Excluded Materials", desc = "Add resources that should never be chosen for node expansion. Exclusions also apply to waypoints with an explicit material preference. List order does not matter.")
    @Expose var excludedMaterials: MutableList<MiningNodeMaterial> = mutableListOf()
)

/** Immutable context used by matching and cache invalidation, independent of mutable config lists. */
data class NodeExpansionPolicy(
    val dwarvenMaterials: Boolean,
    val inCrystalHollows: Boolean,
    val allowedMaterials: Set<MiningNodeMaterial>,
)
