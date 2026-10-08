package github.businessdirt.eurybium.api.events

import github.businessdirt.eurybium.data.model.IslandType
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class HandleEvent(

    /**
     * For cases where multiple events are listened to, and properties are unnecessary.
     */
    val events: Array<KClass<out EurybiumEvent>> = [],

    /**
     * If the event should only be received while on SkyBlock.
     */
    val onlyOnSkyBlock: Boolean = false,

    /**
     * If the event should only be received while being on specific SkyBlock islands.
     */
    val onIslands: Array<IslandType> = [],

    /**
     * The priority of when the event will be called, lower priority will be called first, see the companion object.
     */
    val priority: Int = 0,

    /**
     * If the event is cancelled & receiveCancelled is true, then the method will still invoke.
     */
    val receiveCancelled: Boolean = false
) {
    @Suppress("unused")
    companion object {
        const val HIGHEST = -2
        const val HIGH = -1
        const val MEDIUM = 0
        const val LOW = 1
        const val LOWEST = 2
    }
}
