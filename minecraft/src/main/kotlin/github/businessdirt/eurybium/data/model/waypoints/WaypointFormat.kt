package github.businessdirt.eurybium.data.model.waypoints

/**
 * Imports and exports an external ordered-waypoint format.
 *
 * Implementations can be discovered with `ServiceLoader.load(WaypointFormat::class.java)`.
 * Register a public class with a public no-argument constructor using `@AutoService`.
 */
interface WaypointFormat {

    /** Stable identifier used to select the format, such as `coleweight`. */
    val name: String

    /** Loads a complete route, or returns null for unsupported or invalid waypoint data. */
    fun load(string: String): Waypoints<EurybiumWaypoint>?

    /** Checks whether [load] accepts the input. Use [load] directly if the route is also needed. */
    fun canLoad(string: String): Boolean = load(string) != null

    /** Encodes the route without modifying its waypoints or options. */
    fun export(waypoints: Waypoints<EurybiumWaypoint>): String
}
