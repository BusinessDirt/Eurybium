package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.annotations.Expose
import net.minecraft.core.BlockPos

/**
 * An ordered waypoint at a block position, with additional format-specific [options].
 *
 * [number] is the route's waypoint number. [location] is stored as an immutable position,
 * even when the caller supplies a mutable BlockPos.
 */
class EurybiumWaypoint(
    location: BlockPos,
    @Expose var number: Int,
    @Expose val options: MutableMap<String, String> = mutableMapOf(),
) : Copyable<EurybiumWaypoint> {

    @Expose
    val location: BlockPos = location.immutable()

    /**
     * Optional block ID identifying the desired node, for example `minecraft:magenta_stained_glass`
     * or `minecraft:diamond_ore`. Glass panes and deepslate variants match their base material.
     * Null selects the nearest ore or colored glass. Stored in options for format compatibility.
     */
    var nodeMaterial: String?
        get() = options["nodeMaterial"]
        set(value) {
            if (value.isNullOrBlank()) options.remove("nodeMaterial")
            else options["nodeMaterial"] = value.trim().lowercase()
        }

    /** Copies editable data independently, including the optional node material. */
    override fun copy(): EurybiumWaypoint = EurybiumWaypoint(location, number, options.toMutableMap())
}
