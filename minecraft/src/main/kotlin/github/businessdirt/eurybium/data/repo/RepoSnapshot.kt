package github.businessdirt.eurybium.data.repo

import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.MineshaftType
import java.util.Collections

/** Coordinates remain independent of the current world until a feature resolves their space. */
data class RepoPosition(val x: Int, val y: Int, val z: Int)

enum class RepoCoordinateSpace { WORLD, TEMPLATE }

/** Location restrictions shared by routes and mining nodes; template placements need runtime resolution. */
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

/**
 * A surveyed gemstone cluster in one mineshaft variant, using fixed world coordinates.
 * [sourceFile] retains file membership for debug commands without depending on ID conventions.
 */
class RepoMiningNode(
    val id: String,
    val mineshaft: MineshaftType,
    val material: String,
    blocks: List<RepoPosition>,
    blockTypes: List<String>,
    val sourceFile: String,
) {
    val blocks: List<RepoPosition> = java.util.List.copyOf(blocks)
    val blockTypes: List<String> = java.util.List.copyOf(blockTypes)
}

/** One validated revision, published atomically. Containers cannot be mutated by event consumers. */
class RepoSnapshot(
    val revision: String,
    val fetchedAtMillis: Long,
    patterns: Map<String, RepoPatternData>,
    routes: Map<String, RepoRoute>,
    nodes: Map<String, RepoMiningNode>,
) {
    val patterns: Map<String, RepoPatternData> = Collections.unmodifiableMap(LinkedHashMap(patterns))
    val routes: Map<String, RepoRoute> = Collections.unmodifiableMap(LinkedHashMap(routes))
    val nodes: Map<String, RepoMiningNode> = Collections.unmodifiableMap(LinkedHashMap(nodes))

    companion object {
        val EMPTY = RepoSnapshot("", 0, emptyMap(), emptyMap(), emptyMap())
    }
}
