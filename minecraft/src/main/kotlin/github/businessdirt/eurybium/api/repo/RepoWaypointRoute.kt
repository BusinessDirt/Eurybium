package github.businessdirt.eurybium.api.repo

import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import github.businessdirt.eurybium.data.model.waypoints.Waypoints
import github.businessdirt.eurybium.data.repo.RepoCoordinateSpace
import github.businessdirt.eurybium.data.repo.RepoPosition
import github.businessdirt.eurybium.data.repo.RepoRoute
import github.businessdirt.eurybium.data.repo.RepoSnapshot
import net.minecraft.core.BlockPos

/** A live route lookup. Each conversion creates independent editable waypoints; repo data stays immutable. */
class RepoWaypointRoute(val id: String) {
    fun resolve(snapshot: RepoSnapshot = RepoAPI.snapshot): RepoRoute? = snapshot.routes[id]

    /**
     * World-space routes need no transform. Template routes require an explicit, verified placement;
     * returning null prevents accidentally using local template coordinates in the current world.
     */
    fun waypoints(
        snapshot: RepoSnapshot = RepoAPI.snapshot,
        transform: ((RepoPosition) -> BlockPos)? = null,
    ): Waypoints<EurybiumWaypoint>? {
        val route = resolve(snapshot) ?: return null
        if (route.scope.space == RepoCoordinateSpace.TEMPLATE && transform == null) return null
        val points = route.points.mapIndexed { index, point ->
            val position = if (route.scope.space == RepoCoordinateSpace.TEMPLATE) transform!!(point) else BlockPos(point.x, point.y, point.z)
            EurybiumWaypoint(position, index + 1)
        }
        return Waypoints(points.toMutableList())
    }
}
