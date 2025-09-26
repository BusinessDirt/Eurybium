package github.businessdirt.eurybium.data.model.waypoints

interface WaypointFormat {
    fun load(string: String): Waypoints<EurybiumWaypoint>?
    fun canLoad(string: String): Boolean
    fun export(waypoints: Waypoints<EurybiumWaypoint>): String
    val name: String
}