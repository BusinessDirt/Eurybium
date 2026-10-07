package github.businessdirt.eurybium.features.mining.waypoints

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.config.features.mining.OrderedWaypointsConfig
import github.businessdirt.eurybium.config.manager.ConfigFileType
import github.businessdirt.eurybium.core.concurrency.BackgroundTasks
import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import github.businessdirt.eurybium.data.model.waypoints.WaypointFormat
import github.businessdirt.eurybium.data.model.waypoints.Waypoints
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderLastEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.minecraft.core.BlockPos
import java.util.Locale
import java.util.ServiceLoader

/**
 * Commands, persistence, and rendering for the active ordered route.
 *
 * Route state and clipboard access belong to the client thread. Clipboard parsing and export
 * encoding run in the background using independent data; results return through Minecraft's executor.
 */
@EurybiumModule
object OrderedWaypoints {

    private val config get() = EurybiumMod.config.mining.orderedWaypoints
    private val route = OrderedWaypointRoute()
    private var loadRevision = 0L

    // Providers are stateless and discovered once, including when queried by command suggestions.
    private val formats by lazy {
        ServiceLoader.load(WaypointFormat::class.java, WaypointFormat::class.java.classLoader).toList()
    }

    @HandleEvent
    fun onWorldRenderLastEvent(event: WorldRenderLastEvent) {
        if (!config.enabled) return
        val player = getMinecraft().player ?: return
        route.advanceIfNear(player.position(), config.waypointRange.toDouble())

        for (index in route.visibleIndices()) {
            val color = when (index) {
                route.currentIndex -> config.currentWaypointColor
                route.nextIndex -> config.nextWaypointColor
                else -> config.previousWaypointColor
            }

            val waypoint = route.waypoints[index]
            when (config.renderMode) {
                OrderedWaypointsConfig.RenderMode.FILL -> event.drawWaypointFilled(waypoint, color, true)
                OrderedWaypointsConfig.RenderMode.OUTLINE -> event.drawWaypointOutlined(waypoint, color, config.blockOutlineThickness.toInt(), false)
                OrderedWaypointsConfig.RenderMode.GLOW -> event.drawWaypointGlowing(waypoint, color)
            }
        }

        if (config.traceLine && route.waypoints.size > 1) {
            event.drawLineToEye(
                route.waypoints[route.nextIndex].location.center,
                config.traceLineColor,
                config.traceLineThickness.toInt(),
                depth = true,
            )
        }
    }

    /** A route is local to its world; also invalidates clipboard imports still being parsed. */
    @HandleEvent
    fun onWorldChangeEvent(event: WorldChangeEvent) = unload(sendMessage = false)

    /** Returns a snapshot suitable for command suggestions. */
    fun getRouteNames(): List<String> = EurybiumMod.orderedWaypointsRoutes.routes?.keys?.toList().orEmpty()

    /** Loads a named saved route, or parses the clipboard when [name] is blank. */
    fun load(name: String) {
        val revision = ++loadRevision
        if (name.isNotBlank()) {
            val saved = EurybiumMod.orderedWaypointsRoutes.routes?.get(name)
            if (saved == null) {
                EurybiumMod.logger.error("Route '{}' does not exist. Saved routes: {}", name, getRouteNames().joinToString(", "))
                return
            }

            route.load(saved)
            EurybiumMod.logger.info("Loaded ordered waypoints from '{}'.", name)

            return
        }

        val minecraft = getMinecraft()
        val clipboard = minecraft.keyboardHandler.clipboard

        BackgroundTasks.launchIO("load-waypoints") {
            val imported = loadWaypoints(clipboard)
            minecraft.execute {
                // A newer load, unload, edit, or world change takes precedence over this result.
                if (revision == loadRevision) {
                    if (imported == null) {
                        EurybiumMod.logger.error("Cannot parse waypoints. Supported formats: {}", getWaypointFormats().joinToString(", "))
                    } else {
                        route.load(imported)
                        EurybiumMod.logger.info("Loaded ordered waypoints from clipboard.")
                    }
                }
            }
        }
    }

    /** Clears the active route without changing any saved routes. */
    fun unload(sendMessage: Boolean = true) {
        loadRevision++
        route.clear()
        if (sendMessage) EurybiumMod.logger.info("Unloaded ordered waypoints.")
    }

    /** Moves forward [amount] positions, wrapping around the route. */
    fun skip(amount: Int) = move(amount, backwards = false)

    /** Moves backward [amount] positions, wrapping around the route. */
    fun unskip(amount: Int) = move(amount, backwards = true)

    private fun move(amount: Int, backwards: Boolean) {
        if (amount < 1) {
            EurybiumMod.logger.error("Waypoint count must be at least 1.")
            return
        }

        if (!route.move(if (backwards) -amount.toLong() else amount.toLong())) {
            EurybiumMod.logger.error("There are no waypoints to navigate.")
            return
        }

        loadRevision++
        EurybiumMod.logger.info("{} {} waypoint(s).", if (backwards) "Unskipped" else "Skipped", amount)
    }

    /** Selects a one-based position in the active route. */
    fun skipTo(number: Int) {
        if (!route.skipTo(number)) {
            EurybiumMod.logger.error("Waypoint number must be between 1 and {}.", route.waypoints.size)
            return
        }

        loadRevision++
        EurybiumMod.logger.info("Skipped to waypoint {}.", number)
    }

    /** Deletes a one-based route position and renumbers the remaining waypoints. */
    fun delete(number: Int) {
        if (!route.delete(number)) {
            EurybiumMod.logger.error("Waypoint number must be between 1 and {}.", route.waypoints.size)
            return
        }

        loadRevision++
        EurybiumMod.logger.info("Removed waypoint {}.", number)
    }

    /** Inserts a waypoint at the block beneath the player; does nothing when no player exists. */
    fun add(number: Int) {
        val player = getMinecraft().player
        if (player == null) {
            EurybiumMod.logger.error("Join a world before adding waypoints.")
            return
        }

        val position = BlockPos.containing(player.position().add(0.0, -1.0, 0.0))
        if (!route.add(number, position)) {
            EurybiumMod.logger.error("Insertion number must be between 1 and {}.", route.waypoints.size + 1)
            return
        }

        loadRevision++
        EurybiumMod.logger.info("Inserted waypoint {} at {}, {}, {}.", number, position.x, position.y, position.z)
    }

    /** Exports a stable snapshot, then writes the clipboard on the client thread. */
    fun export(format: String) {
        val selected = format.ifBlank { "coleweight" }.lowercase(Locale.ROOT)
        val provider = formats.firstOrNull { it.name == selected }
        if (provider == null) {
            EurybiumMod.logger.error("Unknown waypoint format '{}'. Formats: {}", format, getWaypointFormats().joinToString(", "))
            return
        }

        val snapshot = route.waypoints.deepCopy()
        val minecraft = getMinecraft()

        BackgroundTasks.launchIO("export-waypoints") {
            val encoded = provider.export(snapshot)
            minecraft.execute {
                minecraft.keyboardHandler.clipboard = encoded
                EurybiumMod.logger.info("Route was copied to clipboard.")
            }
        }
    }

    /** Persists an independent snapshot, initializing the route map even before joining Hypixel. */
    fun save(name: String) {
        if (name.isBlank()) {
            EurybiumMod.logger.error("Route name must not be blank.")
            return
        }

        val routes = EurybiumMod.orderedWaypointsRoutes.routes
            ?: mutableMapOf<String, Waypoints<EurybiumWaypoint>>().also { EurybiumMod.orderedWaypointsRoutes.routes = it }

        routes[name] = route.waypoints.deepCopy()
        saveRoutes()

        EurybiumMod.logger.info("Route saved as '{}'. Use /eybo load to select it.", name)
    }

    /** Deletes a saved route without unloading the active copy. */
    fun erase(name: String) {
        if (EurybiumMod.orderedWaypointsRoutes.routes?.remove(name) == null) {
            EurybiumMod.logger.error("Route '{}' does not exist.", name)
            return
        }

        saveRoutes()
        EurybiumMod.logger.info("Route '{}' deleted.", name)
    }

    private fun saveRoutes() = EurybiumMod.configManager.saveConfig(ConfigFileType.ROUTES, "waypoint-route-edit")

    /** Returns the first complete route accepted by a registered format, or null if none accepts it. */
    fun loadWaypoints(data: String): Waypoints<EurybiumWaypoint>? = formats.firstNotNullOfOrNull { it.load(data) }

    /** Encodes [waypoints] using the selected registered format without modifying the route. */
    fun exportWaypoints(waypoints: Waypoints<EurybiumWaypoint>, name: String): String? =
        formats.firstOrNull { it.name == name.lowercase(Locale.ROOT) }?.export(waypoints)

    /** Lists the stable IDs of the cached AutoService providers. */
    fun getWaypointFormats(): List<String> = formats.map { it.name }
}
