package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.annotations.Expose

/** A value that can produce an independent copy of its editable state. */
interface Copyable<T> {

    /** Returns a copy whose editable state is not shared with this value. */
    fun copy(): T
}

/**
 * An editable, ordered route backed by [waypoints]. List operations modify that same backing list.
 *
 * Use [deepCopy] when edits must not affect the original route. Gson's collection adapter
 * serializes this list-like container as a JSON array.
 */
@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
class Waypoints<T : Copyable<T>>(
    @Expose val waypoints: MutableList<T> = mutableListOf(),
) : MutableList<T> by waypoints {

    /** Copies the list and each waypoint, including the waypoint's editable options. */
    fun deepCopy(): Waypoints<T> = transform { it.copy() }

    /** Converts each waypoint into a new route, preserving order and leaving this list unchanged. */
    inline fun <R : Copyable<R>> transform(transform: (T) -> R): Waypoints<R> =
        Waypoints(waypoints.map(transform).toMutableList())
}
