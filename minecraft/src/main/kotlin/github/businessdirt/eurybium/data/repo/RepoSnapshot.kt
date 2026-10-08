package github.businessdirt.eurybium.data.repo

import github.businessdirt.eurybium.data.model.IslandType
import java.util.Collections

/** A repository route point with an optional mining material preference. */
data class RepoPosition(val x: Int, val y: Int, val z: Int, val nodeMaterial: String? = null)

enum class RepoCoordinateSpace { WORLD, TEMPLATE }

/** Location restrictions for routes; template placements need runtime resolution. */
data class RepoScope(
    val island: IslandType,
    val region: String?,
    val mineshaft: String?,
    val space: RepoCoordinateSpace,
    val layout: String?,
)

/** A validated pattern compiled once when its repository snapshot is prepared. */
data class RepoPatternData(val id: String, val source: String, val regex: Regex)

/** Immutable ordered route data. Consumers must copy it into editable waypoint state. */
class RepoRoute(val id: String, val scope: RepoScope, points: List<RepoPosition>) {
    val points: List<RepoPosition> = java.util.List.copyOf(points)
}

/** One validated revision, published atomically. Containers cannot be mutated by event consumers. */
class RepoSnapshot(
    val revision: String,
    val fetchedAtMillis: Long,
    patterns: Map<String, RepoPatternData>,
    routes: Map<String, RepoRoute>,
) {
    val patterns: Map<String, RepoPatternData> = Collections.unmodifiableMap(LinkedHashMap(patterns))
    val routes: Map<String, RepoRoute> = Collections.unmodifiableMap(LinkedHashMap(routes))

    companion object {
        val EMPTY = RepoSnapshot("", 0, emptyMap(), emptyMap())
    }
}
