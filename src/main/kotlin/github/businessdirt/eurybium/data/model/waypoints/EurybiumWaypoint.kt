package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.annotations.Expose
import net.minecraft.util.math.BlockPos

class EurybiumWaypoint(
    @Expose val location: BlockPos,
    @Expose var number: Int,
    @Expose val options: MutableMap<String, String> = mutableMapOf(),
) : Copyable<EurybiumWaypoint> {
    override fun copy() = EurybiumWaypoint(location, number, options)


}