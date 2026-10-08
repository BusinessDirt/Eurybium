package github.businessdirt.eurybium.data.repo

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.MiningNodeMaterial
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.data.model.waypoints.MiningRouteIds.internalRouteId
import github.businessdirt.eurybium.data.model.waypoints.MiningRouteIds

/** Strict validation happens off-thread before a revision can replace the last-known-good data. */
internal object RepoParser {

    const val MAX_FILE_BYTES = 8 * 1024 * 1024
    const val MAX_TOTAL_FILE_BYTES = 24 * 1024 * 1024
    val requiredFiles = listOf("patterns/chat.json", "patterns/scoreboard.json")
    val optionalFiles = listOf("mining/routes.json")

    private val shaftRouteIds = MineshaftType.entries.map { it.internalRouteId }.toSet()
    private val spawningRouteIds = setOf(
        MiningRouteIds.SHAFT_SPAWN_MITHRIL,
        MiningRouteIds.SHAFT_SPAWN_TUNGSTEN,
        MiningRouteIds.SHAFT_SPAWN_GEMSTONES,
    )

    private val revisionPattern = Regex("[a-fA-F0-9]{40}")
    private val idPattern = Regex("[A-Za-z0-9_.:/-]{1,128}")

    fun validRevision(revision: String): Boolean = revisionPattern.matches(revision)

    /** Missing optional catalogs are empty; malformed present catalogs reject the whole candidate. */
    fun parse(revision: String, fetchedAtMillis: Long, files: Map<String, String>): RepoSnapshot {
        require(validRevision(revision)) { "Invalid repository commit" }
        require(fetchedAtMillis >= 0) { "Invalid repository timestamp" }
        require(files.keys.all { it in requiredFiles || it in optionalFiles }) { "Unknown repository file" }
        require(requiredFiles.all { it in files }) { "Missing pattern catalog" }
        require(files.values.sumOf { it.toByteArray(Charsets.UTF_8).size.toLong() } <= MAX_TOTAL_FILE_BYTES) {
            "Repository data is too large"
        }

        val patterns = linkedMapOf<String, RepoPatternData>()
        for (path in requiredFiles) {
            for (entry in records(files.getValue(path), "patterns")) {
                val id = entry.id()
                val source = entry.string("pattern")
                require(source.length <= 4096) { "Pattern $id is too long" }
                require(patterns.put(id, RepoPatternData(id, source, Regex(source))) == null) { "Duplicate pattern $id" }
            }
        }

        val routes = linkedMapOf<String, RepoRoute>()
        files["mining/routes.json"]?.let { text ->
            val allowed = shaftRouteIds + spawningRouteIds
            for (entry in records(text, "routes")) {
                val id = entry.id()
                require(id in allowed) { "Unknown built-in route $id" }
                val scope = scope(entry)
                if (id !in spawningRouteIds) {
                    require(scope.island == IslandType.MINESHAFT && scope.mineshaft == id.removePrefix(MiningRouteIds.NAMESPACE)) {
                        "Route $id has a mismatched shaft scope"
                    }
                } else {
                    require(scope.island == IslandType.DWARVEN_MINES && scope.region == "DWARVEN_BASE_CAMP") {
                        "Spawning route has a mismatched scope"
                    }
                }
                require(routes.put(id, RepoRoute(id, scope, routePoints(entry))) == null) { "Duplicate route $id" }
            }
        }

        return RepoSnapshot(revision, fetchedAtMillis, patterns, routes)
    }

    private fun document(text: String): JsonObject {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_FILE_BYTES) { "Repository file is too large" }
        val root = JsonParser.parseString(text).asJsonObject
        require(root.get("schemaVersion")?.integer() == 1) { "Unsupported repository schema" }
        return root
    }

    private fun records(text: String, key: String): List<JsonObject> = records(document(text), key)

    private fun records(root: JsonObject, key: String): List<JsonObject> {
        val array = root.getAsJsonArray(key) ?: error("Missing $key array")
        require(array.size() <= 20_000) { "Too many $key records" }
        return array.map { it.asJsonObject }
    }

    private fun scope(entry: JsonObject): RepoScope {
        val island = IslandType.valueOf(entry.string("island"))
        require(island.isValidIsland()) { "Invalid repository island" }

        val space = RepoCoordinateSpace.valueOf(entry.string("space"))
        val layout = entry.optionalString("layout")
        require(space != RepoCoordinateSpace.TEMPLATE || layout != null) { "Template needs a layout ID" }

        val shaft = entry.optionalString("mineshaft")
        require(shaft == null || "eurybium:$shaft" in shaftRouteIds) { "Unknown mineshaft $shaft" }
        require(island != IslandType.MINESHAFT || shaft != null) { "Mineshaft data needs a shaft variant" }

        return RepoScope(island, entry.optionalString("region"), shaft, space, layout)
    }

    private fun routePoints(entry: JsonObject): List<RepoPosition> {
        val array = entry.getAsJsonArray("points") ?: error("Missing route points")
        require(array.size() in 1..10_000) { "Invalid route point count" }

        return array.map { item ->
            val point = item.asJsonArray
            require(point.size() in 3..4) {
                "Coordinates need three integers and route points may include a material string"
            }
            val material = if (point.size() == 4 && !point[3].isJsonNull) {
                require(point[3].isJsonPrimitive && point[3].asJsonPrimitive.isString) { "Expected node material string" }
                point[3].asString.takeIf { it.isNotBlank() }?.let {
                    requireNotNull(MiningNodeMaterial.fromId(it)) { "Unknown node material $it" }.id
                }
            } else null

            RepoPosition(point[0].integer(), point[1].integer(), point[2].integer(), material)
        }
    }

    private fun JsonElement.integer(): Int {
        require(isJsonPrimitive && asJsonPrimitive.isNumber) { "Expected integer" }
        return asBigDecimal.intValueExact()
    }

    private fun JsonObject.string(key: String): String {
        val value = get(key) ?: error("Missing $key")
        require(value.isJsonPrimitive && value.asJsonPrimitive.isString) { "Expected string for $key" }

        return value.asString
    }

    private fun JsonObject.optionalString(key: String): String? = get(key)?.takeUnless { it.isJsonNull }?.let {
        string(key).also { value -> require(idPattern.matches(value)) { "Invalid $key" } }
    }

    private fun JsonObject.id(): String = string("id").also { require(idPattern.matches(it)) { "Invalid ID" } }
}
