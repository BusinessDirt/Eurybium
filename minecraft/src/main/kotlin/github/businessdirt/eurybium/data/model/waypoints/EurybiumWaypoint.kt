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

    /** Runtime node lookup cache; excluded from JSON and reset when copying a waypoint. */
    @Transient
    var nearestNodeIndex: Int? = null

    /** Copies editable data independently, leaving node lookup to the new waypoint's owner. */
    override fun copy(): EurybiumWaypoint = EurybiumWaypoint(location, number, options.toMutableMap())
}
