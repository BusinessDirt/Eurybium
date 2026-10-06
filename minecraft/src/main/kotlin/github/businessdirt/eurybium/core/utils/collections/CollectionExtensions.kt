package github.businessdirt.eurybium.core.utils.collections

import java.util.*

object CollectionExtensions {

    inline fun <reified E, reified L : MutableCollection<E>> Queue<E>.drainTo(list: L): L {
        while (true) list.add(this.poll() ?: break)
        return list
    }
}
