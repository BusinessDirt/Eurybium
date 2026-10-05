package github.businessdirt.eurybium.core.utils

import java.util.Queue

object CollectionUtils {

    inline fun <reified E, reified L : MutableCollection<E>> Queue<E>.drainTo(list: L): L {
        while (true) list.add(this.poll() ?: break)
        return list
    }
}
