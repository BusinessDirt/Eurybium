package github.businessdirt.eurybium.core.utils.collections

import java.util.Queue

/** Small collection operations that do not depend on Minecraft. */
object CollectionExtensions {

    /**
     * Removes queue entries into [destination], preserving poll order, and returns the destination.
     * The queue must not contain null entries because [Queue.poll] uses null to signal exhaustion.
     */
    fun <E, C : MutableCollection<in E>> Queue<E>.drainTo(destination: C): C {
        while (true) {
            val entry = poll() ?: break
            destination.add(entry)
        }

        return destination
    }
}
