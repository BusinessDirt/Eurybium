package github.businessdirt.eurybium.api.events

import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI
import github.businessdirt.eurybium.data.model.IslandType
import java.util.function.Consumer
import kotlin.collections.isNotEmpty
import kotlin.collections.toSet

typealias EventPredicate = (event: EurybiumEvent) -> Boolean

class EurybiumEventListener(
    val name: String,
    val invoker: Consumer<EurybiumEvent>,
    options: HandleEvent,
    extraPredicates: List<EventPredicate> = listOf()
) {
    val priority: Int = options.priority
    val canReceiveCancelled: Boolean = options.receiveCancelled


    @Suppress("JoinDeclarationAndAssignment")
    private val cachedPredicates: List<EventPredicate>
    private val predicates: List<EventPredicate>

    init {
        this.cachedPredicates = buildList {
            if (options.onlyOnSkyblock) add { _ -> HypixelLocationAPI.inSkyBlock && HypixelLocationAPI.inHypixel }
            if (options.onlyOnIsland != IslandType.ANY) add { _ -> options.onlyOnIsland.isInIsland() }
            if (options.onlyOnIslands.isNotEmpty()) {
                val set = options.onlyOnIslands.toSet()
                add { _ -> HypixelLocationAPI.inAnyIsland(set) }
            }
        }

        this.predicates = buildList {
            if (!canReceiveCancelled) add { event -> !event.isCancelled }
            addAll(extraPredicates)
        }
    }

    fun shouldInvoke(event: EurybiumEvent): Boolean {
        return cachedPredicates.all { it(event) } && predicates.all { it(event) }
    }
}
