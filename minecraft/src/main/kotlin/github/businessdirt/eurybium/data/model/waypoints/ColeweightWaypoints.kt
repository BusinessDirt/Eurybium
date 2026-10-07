package github.businessdirt.eurybium.data.model.waypoints

import com.google.auto.service.AutoService
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import github.businessdirt.eurybium.config.manager.ConfigManager
import github.businessdirt.eurybium.core.json.adapters.KSerializable
import net.minecraft.core.BlockPos
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Type

/** Coleweight's JSON waypoint representation; RGB components use the 0–1 scale. */
@KSerializable
data class ColeweightWaypoint(
    @Expose val x: Int,
    @Expose val y: Int,
    @Expose val z: Int,
    @Expose val r: Double,
    @Expose val g: Double,
    @Expose val b: Double,
    @Expose val options: MutableMap<String, String> = mutableMapOf(),
) : Copyable<ColeweightWaypoint> {

    /** Copies the options map rather than sharing it with the original waypoint. */
    override fun copy(): ColeweightWaypoint = ColeweightWaypoint(x, y, z, r, g, b, options.toMutableMap())
}

/**
 * Converts Coleweight JSON arrays to ordered Eurybium waypoints.
 *
 * Coleweight stores the waypoint number in `options["name"]`. Imports require an integer
 * name for every waypoint; export uses the current number and the legacy green RGB colour.
 */
@AutoService(WaypointFormat::class)
class ColeweightWaypointFormat : WaypointFormat {

    override val name: String = "coleweight"

    override fun load(string: String): Waypoints<EurybiumWaypoint>? {
        val imported = try {
            ConfigManager.gson.fromJson<List<ColeweightWaypoint?>>(string, waypointListType) ?: return null
        } catch (exception: Exception) {
            // Gson and reflective Kotlin constructor calls report invalid fields differently.
            // Keep the failure boundary limited to parsing, including explicit null violations.
            logger.debug("Invalid Coleweight waypoint data", exception)
            return null
        }

        val waypoints = mutableListOf<EurybiumWaypoint>()
        for (waypoint in imported) {
            if (waypoint == null) return null
            val number = waypoint.options["name"]?.toIntOrNull() ?: return null
            val location = BlockPos(waypoint.x, waypoint.y, waypoint.z)
            waypoints += EurybiumWaypoint(location, number, waypoint.options.toMutableMap())
        }
        return Waypoints(waypoints)
    }

    override fun export(waypoints: Waypoints<EurybiumWaypoint>): String {
        val exported = waypoints.map { waypoint ->
            // Derive name from the editable number without changing the original options.
            val options = waypoint.options.toMutableMap().apply { put("name", waypoint.number.toString()) }
            with(waypoint.location) { ColeweightWaypoint(x, y, z, 0.0, 1.0, 0.0, options) }
        }
        return ConfigManager.gson.toJson(exported, waypointListType)
    }

    private companion object {
        val logger: Logger = LogManager.getLogger(ColeweightWaypointFormat::class.java)
        val waypointListType: Type = object : TypeToken<List<ColeweightWaypoint>>() {}.type
    }
}
