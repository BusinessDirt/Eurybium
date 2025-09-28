package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.annotations.Expose

interface Copyable<T> {
    fun copy(): T
}

class Waypoints<T : Copyable<T>>(
    @Expose
    val waypoints: MutableList<T> = mutableListOf(),
) : MutableList<T> by waypoints {

    fun deepCopy(): Waypoints<T> {
        return transform { it.copy() }
    }

    inline fun <R : Copyable<R>> transform(transform: (T) -> R): Waypoints<R> {
        return Waypoints(waypoints.map { transform(it) }.toMutableList())
    }
}