package github.businessdirt.eurybium.api.events

import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI
import github.businessdirt.eurybium.data.model.IslandType
import java.util.function.Consumer

/** Additional per-event eligibility check, evaluated only after built-in filters pass. */
typealias EventPredicate = (event: EurybiumEvent) -> Boolean

/**
 * An event callback with its priority, cancellation policy, and location filters.
 *
 * Additional predicates and island selections are copied at construction. Location is sampled
 * once per [shouldInvoke] call, so all filters evaluate the same published update.
 */
class EurybiumEventListener(
    val name: String,
    val invoker: Consumer<EurybiumEvent>,
    private val options: HandleEvent,
    extraPredicates: List<EventPredicate> = emptyList(),
) {
    val priority: Int = options.priority
    val canReceiveCancelled: Boolean = options.receiveCancelled

    private val islands = options.onlyOnIslands.toSet()
    private val predicates = extraPredicates.toList()

    /** Checks cancellation and location restrictions before short-circuiting through extra predicates. */
    fun shouldInvoke(event: EurybiumEvent): Boolean {
        if (event.isCancelled && !canReceiveCancelled) return false

        // Retain one snapshot so a location update cannot mix old and new fields across these checks.
        val location = HypixelLocationAPI.state
        if (options.onlyOnSkyblock && !location.inSkyBlock) return false
        if (options.onlyOnIsland != IslandType.ANY && !location.inAnyIsland(listOf(options.onlyOnIsland))) return false
        if (islands.isNotEmpty() && !location.inAnyIsland(islands)) return false

        return predicates.all { it(event) }
    }
}
