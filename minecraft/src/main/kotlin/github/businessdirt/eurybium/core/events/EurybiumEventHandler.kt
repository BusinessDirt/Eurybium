package github.businessdirt.eurybium.core.events

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.utils.StringUtils.optionalAn
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.reflect.KClass

class EurybiumEventHandler private constructor(
    val name: String,
    private val listeners: List<EurybiumEventListener>,
    private val canReceiveCancelled: Boolean
) {

    constructor(event: KClass<out EurybiumEvent>, listeners: List<EurybiumEventListener>) : this(
        (event.simpleName?.split(".")?.lastOrNull() ?: event.simpleName)?.replace("$", ".") ?: "Unknown",
        listeners.sortedBy { it.priority }.toList(),
        listeners.any { it.canReceiveCancelled }
    )

    fun post(event: EurybiumEvent, onError: ((Throwable) -> Unit)?): Boolean {
        if (this.listeners.isEmpty()) return event.isCancelled

        for (listener in this.listeners) {
            if (!listener.shouldInvoke(event)) continue

            try {
                listener.invoker.accept(event)
            } catch (throwable: Throwable) {
                val errorName = throwable::class.simpleName ?: "error"
                EurybiumMod.logger.atError()
                    .withThrowable(throwable)
                    .log("Caught ${errorName.optionalAn()} $errorName in ${listener.name} at $name.")

                onError?.invoke(throwable)
            }

            if (event.isCancelled && !this.canReceiveCancelled) break
        }

        return event.isCancelled
    }
}
