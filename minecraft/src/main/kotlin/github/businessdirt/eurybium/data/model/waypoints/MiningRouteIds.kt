package github.businessdirt.eurybium.data.model.waypoints

import github.businessdirt.eurybium.data.model.MineshaftType
import java.util.Locale.getDefault

/** Reserved repository route IDs. Keep these values stable when renaming Kotlin enums or files. */
object MiningRouteIds {

    const val NAMESPACE: String = "eurybium:"
    const val SHAFT_SPAWN_MITHRIL: String = "${NAMESPACE}SHAFT_SPAWN_MITHRIL"
    const val SHAFT_SPAWN_TUNGSTEN: String = "${NAMESPACE}SHAFT_SPAWN_TUNGSTEN"
    const val SHAFT_SPAWN_GEMSTONES: String = "${NAMESPACE}SHAFT_SPAWN_GEMSTONES"

    /** Every shaft variant has a distinct ID; registry membership does not imply repo data exists yet. */
    val MineshaftType.internalRouteId: String
        get() = NAMESPACE + name.replace("_", "").uppercase(getDefault())

    /** User routes must not claim this namespace, including differently cased spellings. */
    fun isReserved(name: String): Boolean = name.startsWith(NAMESPACE, ignoreCase = true)
}
