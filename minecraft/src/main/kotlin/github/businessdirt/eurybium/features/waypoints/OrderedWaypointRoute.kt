package github.businessdirt.eurybium.features.waypoints

import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import github.businessdirt.eurybium.data.model.waypoints.Waypoints
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

/** Client-thread route state, independent of Minecraft's client and rendering APIs. */
internal class OrderedWaypointRoute {

    val waypoints = Waypoints<EurybiumWaypoint>()

    /** Last reached or selected waypoint; -1 means the loaded route has not reached its first point. */
    var currentIndex: Int = 0
        private set

    val nextIndex: Int get() = if (waypoints.isEmpty()) 0 else (currentIndex + 1) % waypoints.size

    /** Loads an independent route, sorting imported numbers and normalizing them to one-based order, ready to approach the first point. */
    fun load(source: Waypoints<EurybiumWaypoint>) {
        val sorted = source.deepCopy().sortedBy { it.number }
        waypoints.clear()
        waypoints.addAll(sorted)
        renumber()
        currentIndex = if (waypoints.isEmpty()) 0 else -1
    }

    /** Clears the route and resets navigation. */
    fun clear() {
        waypoints.clear()
        currentIndex = 0
    }

    /** Moves in either direction with wraparound; long arithmetic prevents overflow for large skips. */
    fun move(amount: Long): Boolean {
        if (waypoints.isEmpty()) return false
        val size = waypoints.size.toLong()
        currentIndex = Math.floorMod(currentIndex.toLong() + amount % size, size).toInt()
        return true
    }

    /** Selects a one-based route position, returning false if it does not exist. */
    fun skipTo(number: Int): Boolean {
        if (number !in 1..waypoints.size) return false
        currentIndex = number - 1
        return true
    }

    /** Inserts a waypoint while retaining the previously selected waypoint where possible. */
    fun add(number: Int, location: BlockPos): Boolean {
        if (number !in 1..waypoints.size + 1) return false
        val index = number - 1
        if (waypoints.isNotEmpty() && index <= currentIndex) currentIndex++
        waypoints.add(index, EurybiumWaypoint(location, number))
        renumber()
        return true
    }

    /** Deletes a waypoint, selecting its successor if the current waypoint is removed. */
    fun delete(number: Int): Boolean {
        if (number !in 1..waypoints.size) return false
        val index = number - 1
        waypoints.removeAt(index)
        if (index < currentIndex) currentIndex--
        if (waypoints.isEmpty() || currentIndex >= waypoints.size) currentIndex = 0
        renumber()
        return true
    }

    /** Only the first target before starting; otherwise previous, current, and next without duplicates. */
    fun visibleIndices(): List<Int> {
        if (waypoints.isEmpty()) return emptyList()
        if (currentIndex < 0) return listOf(nextIndex)
        val previous = Math.floorMod(currentIndex - 1, waypoints.size)
        return listOf(previous, currentIndex, nextIndex).distinct()
    }

    /**
     * Advances once when the next target is within range and closer than the current target.
     * Before starting, only the first point needs to be in range.
     * Requiring the next point to be closer prevents two nearby points from alternating every frame.
     * [targetPosition] lets glow mode use the rendered cluster centers without editing saved coordinates.
     */
    fun advanceIfNear(
        playerPosition: Vec3,
        range: Double,
        targetPosition: (EurybiumWaypoint) -> Vec3 = { Vec3.atLowerCornerOf(it.location) },
    ) {
        if (waypoints.isEmpty() || (currentIndex >= 0 && waypoints.size < 2) || !range.isFinite() || range <= 0 || !playerPosition.isFinite) return
        fun distance(index: Int): Double = playerPosition.distanceToSqr(targetPosition(waypoints[index]))
        val nextDistance = distance(nextIndex)
        if (nextDistance < range * range && (currentIndex < 0 || nextDistance < distance(currentIndex))) move(1)
    }

    private fun renumber() {
        for ((index, waypoint) in waypoints.withIndex()) {
            waypoint.number = index + 1
            waypoint.options["name"] = waypoint.number.toString()
        }
    }
}
