package github.businessdirt.eurybium.core.events

/**
 * Use @[HandleEvent]
 */
@Suppress("unused")
abstract class EurybiumEvent {
    var isCancelled: Boolean = false
        private set

    fun post(): Boolean = prePost(onError = null)

    fun post(onError: (Throwable) -> Unit = {}): Boolean = prePost(onError)

    private fun prePost(onError: ((Throwable) -> Unit)?): Boolean {
        return EurybiumEventBus.getEventHandler(this.javaClass).post(this, onError)
    }

    interface Cancellable {
        fun cancel() {
            (this as EurybiumEvent).isCancelled = true
        }
    }
}
