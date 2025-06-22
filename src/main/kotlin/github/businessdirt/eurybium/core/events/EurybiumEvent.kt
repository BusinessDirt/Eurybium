package github.businessdirt.eurybium.core.events

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext

/**
 * Use @[HandleEvent]
 */
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

    interface Rendering {
        val context: WorldRenderContext
    }
}
