package github.businessdirt.eurybium.core.events

import github.businessdirt.eurybium.core.utils.StringUtils.optionalAn
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

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
                logger.atError().withThrowable(throwable).log("Caught $aOrAn $errorName in ${listener.name} at $name.")
                onError?.invoke(throwable)
            }
            if (event.isCancelled && !this.canReceiveCancelled) break
        }

        return event.isCancelled
    }

    companion object {
        val logger: Logger = LogManager.getLogger(EurybiumEventHandler::class.java)
    }
}
