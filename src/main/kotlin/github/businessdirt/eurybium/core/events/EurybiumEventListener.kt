package github.businessdirt.eurybium.core.events

import github.businessdirt.eurybium.core.minecraftevents.ClientEvents
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.utils.SkyBlockUtils
import java.util.function.Consumer

typealias EventPredicate = (event: EurybiumEvent) -> Boolean

class EurybiumEventListener(
    val name: String,
    val invoker: Consumer<Any>,
    options: HandleEvent,
    extraPredicates: List<EventPredicate> = listOf()
) {
    val priority: Int = options.priority
    val canReceiveCancelled: Boolean = options.receiveCancelled

    private var lastTick: Long = -1
    private var cachedPredicateValue = false

    @Suppress("JoinDeclarationAndAssignment")
    private val cachedPredicates: List<EventPredicate>
    private val predicates: List<EventPredicate>

    init {
        this.cachedPredicates = buildList {
            if (options.onlyOnSkyblock) add { _ -> SkyBlockUtils.inSkyblock() }
            if (options.onlyOnIsland != IslandType.ANY) add { _ -> options.onlyOnIsland.isInIsland() }
            if (options.onlyOnIslands.isNotEmpty()) {
                val set = options.onlyOnIslands.toSet()
                add { _ -> SkyBlockUtils.inAnyIsland(set) }
            }
        }

        this.predicates = buildList {
            if (!canReceiveCancelled) add { event -> !event.isCancelled }
            addAll(extraPredicates)
        }
    }

    fun shouldInvoke(event: EurybiumEvent): Boolean {
        if (lastTick != ClientEvents.totalTicks) {
            cachedPredicateValue = cachedPredicates.all { it(event) }
            lastTick = ClientEvents.totalTicks
        }
        return cachedPredicateValue && predicates.all { it(event) }
    }
}
