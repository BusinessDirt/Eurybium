package github.businessdirt.eurybium.data.model.waypoints

import com.google.auto.service.AutoService
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.config.manager.ConfigManager
import net.minecraft.util.math.BlockPos

data class ColeweightWaypoint(
    @Expose val x: Int,
    @Expose val y: Int,
    @Expose val z: Int,
    @Expose val r: Double,
    @Expose val g: Double,
    @Expose val b: Double,
    @Expose val options: MutableMap<String, String> = mutableMapOf(),
) : Copyable<ColeweightWaypoint> {
    override fun copy() = ColeweightWaypoint(x, y, z, r, g, b, options)
}

@AutoService(WaypointFormat::class)
class ColeweightWaypointFormat : WaypointFormat {

    override fun load(string: String): Waypoints<EurybiumWaypoint>? {
        val type = object : TypeToken<Waypoints<ColeweightWaypoint>>() {}.type
        return try {
            ConfigManager.gson.fromJson<Waypoints<ColeweightWaypoint>>(string, type).transform { it.load() }
        } catch (e: Exception) {
            EurybiumMod.logger.debug(e.stackTraceToString())
            null
        }
    }

    private fun ColeweightWaypoint.load() = EurybiumWaypoint(
        BlockPos(x, y, z),
        options["name"]!!.toInt(),
        options
    )

    override fun canLoad(string: String): Boolean {
        return load(string) != null
    }

    override fun export(waypoints: Waypoints<EurybiumWaypoint>): String {
        return ConfigManager.gson.toJson(waypoints.transform { it.export() }, Waypoints<ColeweightWaypoint>()::class.java)
    }

    private fun EurybiumWaypoint.export(): ColeweightWaypoint = with(location) {
        ColeweightWaypoint(x, y, z, 0.0, 1.0, 0.0, options)
    }

    override val name: String get() = "coleweight"
}