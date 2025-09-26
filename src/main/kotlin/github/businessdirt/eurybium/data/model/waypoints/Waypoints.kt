package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.annotations.Expose
import java.util.function.IntFunction

interface Copyable<T> {
    fun copy(): T
}

class Waypoints<T : Copyable<T>>(
    @Expose
    val waypoints: MutableList<T> = mutableListOf(),
) : MutableList<T> by waypoints {
    fun deepCopy() = transform { it.copy() }

    inline fun <R : Copyable<R>> transform(transform: (T) -> R): Waypoints<R> = Waypoints(waypoints.map { transform(it) }.toMutableList())
}