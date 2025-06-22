package github.businessdirt.eurybium.core.events

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.utils.StringUtils.optionalAn
import java.util.function.Consumer
import java.util.function.ToIntFunction
import java.util.stream.Collectors

class EurybiumEventHandler private constructor(
    val name: String,
    private val listeners: List<EurybiumEventListener>,
    private val canReceiveCancelled: Boolean
) {

    constructor(event: Class<EurybiumEvent>, listeners: List<EurybiumEventListener>) : this(
        (event.name.split(".").lastOrNull() ?: event.name).replace("$", "."),
        listeners.sortedBy { it.priority }.toList(),
        listeners.any { it.canReceiveCancelled }
    )

    fun post(event: EurybiumEvent, onError: ((Throwable) -> Unit)?): Boolean {
        if (this.listeners.isEmpty()) return false

        for (listener in this.listeners) {
            if (!listener.shouldInvoke(event)) continue

            try {
                listener.invoker.accept(event)
            } catch (throwable: Throwable) {
                val errorName = throwable::class.simpleName ?: "error"
                val aOrAn = errorName.optionalAn()
                EurybiumMod.logger.error("Caught {} {} in {} at {}: {}", aOrAn, errorName, listener.name, name, throwable.message)
                onError?.invoke(throwable)
            }
            if (event.isCancelled && !this.canReceiveCancelled) break
        }

        return event.isCancelled
    }
}
