package github.businessdirt.eurybium.config.manager

import com.google.gson.JsonElement

/** Small, in-memory migrations applied before loading the main config. */
internal object ConfigMigrations {

    /** Moves mining's former route settings without overwriting a newer top-level category. */
    fun moveOrderedWaypoints(json: JsonElement?) {
        if (json == null || !json.isJsonObject) return
        val root = json.asJsonObject
        val mining = root.get("mining")?.takeIf { it.isJsonObject }?.asJsonObject ?: return
        val legacy = mining.remove("orderedWaypoints") ?: return

        if (!root.has("orderedWaypoints") || root.get("orderedWaypoints").isJsonNull) {
            root.add("orderedWaypoints", legacy)
        }
    }
}
