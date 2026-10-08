package github.businessdirt.eurybium.features.waypoints

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.api.repo.RepoAPI
import github.businessdirt.eurybium.api.repo.RepoWaypointRoute
import github.businessdirt.eurybium.data.model.waypoints.MiningRouteIds
import github.businessdirt.eurybium.config.features.waypoints.OrderedWaypointsConfig
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

    private val config get() = EurybiumMod.config.orderedWaypoints
    private val route = OrderedWaypointRoute()
    private var loadRevision = 0L

    // Providers are stateless and discovered once, including when queried by command suggestions.
    private val formats by lazy {
        ServiceLoader.load(WaypointFormat::class.java, WaypointFormat::class.java.classLoader).toList()
    }

    @HandleEvent
    private fun onWorldRenderLastEvent(event: WorldRenderLastEvent) {
        if (!config.enabled) return
        val player = getMinecraft().player ?: return
        route.advanceIfNear(player.position(), config.waypointRange.toDouble())

        var traceTarget = route.waypoints.getOrNull(route.nextIndex)?.location?.center

        // Reserve the current node first, then next and previous; shared nodes keep current's color.
        val visible = if (config.renderMode == OrderedWaypointsConfig.RenderMode.GLOW) {
            route.visibleIndices().sortedByDescending { index ->
                when (index) {
                    route.currentIndex -> 2
                    route.nextIndex -> 1
                    else -> 0
                }
            }
        } else route.visibleIndices()

        for (index in visible) {
            val color = when (index) {
                route.currentIndex -> config.currentWaypointColor
                route.nextIndex -> config.nextWaypointColor
                else -> config.previousWaypointColor
            }

            val waypoint = route.waypoints[index]
            // Route blocks often overlap solid terrain; both overlays must remain visible through it.
            when (config.renderMode) {
                OrderedWaypointsConfig.RenderMode.FILL -> event.drawWaypointFilled(waypoint, color, depth = false)
                OrderedWaypointsConfig.RenderMode.OUTLINE -> event.drawWaypointOutlined(waypoint, color, config.blockOutlineThickness.toInt(), false)
                OrderedWaypointsConfig.RenderMode.GLOW -> {
                    val priority = when (index) {
                        route.currentIndex -> 2
                        route.nextIndex -> 1
                        else -> 0
                    }
                    val target = event.drawWaypointGlowing(waypoint, color, priority)
                    if (index == route.nextIndex) traceTarget = target
                }
            }
        }

        if (config.traceLine && route.waypoints.isNotEmpty() && (route.currentIndex < 0 || route.waypoints.size > 1)) {
            event.drawLineToEye(
                traceTarget ?: return,
                config.traceLineColor,
                config.traceLineThickness.toInt(),
                depth = true,
            )
        }
    }

    /** A route is local to its world; also invalidates clipboard imports still being parsed. */
    @HandleEvent(events = [ WorldChangeEvent::class ])
    private fun onWorldChangeEvent() = unload(sendMessage = false)

    /** Returns a snapshot suitable for command suggestions. */
    fun getRouteNames(): List<String> = (
        EurybiumMod.orderedWaypointsRoutes.routes?.keys.orEmpty().filterNot(MiningRouteIds::isReserved) + RepoAPI.snapshot.routes.keys
    ).distinct().sorted()

    /** Loads a named saved route, or parses the clipboard when [name] is blank. */
    fun load(name: String, sendErrors: Boolean = true) {
        val revision = ++loadRevision
        if (name.isNotBlank()) {
            val saved = if (MiningRouteIds.isReserved(name)) {
                RepoWaypointRoute(name).waypoints()
            } else {
                EurybiumMod.orderedWaypointsRoutes.routes?.get(name)
            }

            if (saved == null) {
                if (sendErrors) ChatAPI.userError("Route '$name' is unavailable or needs a template placement.")
                return
            }

            route.load(saved)
            ChatAPI.chat("Loaded ordered waypoints from '$name'.")
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
                        if (sendErrors) ChatAPI.userError("Cannot parse waypoints. Supported formats: ${getWaypointFormats().joinToString(", ")}")
                    } else {
                        route.load(imported)
                        ChatAPI.chat("Loaded ordered waypoints from clipboard.")
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
            ChatAPI.userError("Waypoint count must be at least 1.")
            return
        }

        if (!route.move(if (backwards) -amount.toLong() else amount.toLong())) {
            ChatAPI.userError("There are no waypoints to navigate.")
            return
        }

        loadRevision++
        ChatAPI.chat("${if (backwards) "Unskipped" else "Skipped"} $amount waypoint(s).")
    }

    /** Selects a one-based position in the active route. */
    fun skipTo(number: Int) {
        if (!route.skipTo(number)) {
            ChatAPI.userError("Waypoint number must be between 1 and ${route.waypoints.size}.")
            return
        }

        loadRevision++
        EurybiumMod.logger.info("Skipped to waypoint {}.", number)
    }

    /** Deletes a one-based route position and renumbers the remaining waypoints. */
    fun delete(number: Int) {
        if (!route.delete(number)) {
            ChatAPI.userError("Waypoint number must be between 1 and ${route.waypoints.size}.")
            return
        }

        loadRevision++
        ChatAPI.chat("Removed waypoint ${number}.")
    }

    /** Inserts a waypoint at the block beneath the player; does nothing when no player exists. */
    fun add(number: Int) {
        val player = getMinecraft().player
        if (player == null) {
            ChatAPI.userError("Join a world before adding waypoints.")
            return
        }

        val position = BlockPos.containing(player.position().add(0.0, -1.0, 0.0))
        if (!route.add(number, position)) {
            ChatAPI.userError("Insertion number must be between 1 and ${route.waypoints.size + 1}.")
            return
        }

        loadRevision++
        ChatAPI.chat("Inserted waypoint $number at ${position.x}, ${position.y}, ${position.z}.")
    }

    /** Exports a stable snapshot, then writes the clipboard on the client thread. */
    fun export(format: String) {
        val selected = format.ifBlank { "coleweight" }.lowercase(Locale.ROOT)
        val provider = formats.firstOrNull { it.name == selected }
        if (provider == null) {
            ChatAPI.userError("Unknown waypoint format '$format'. Formats: ${getWaypointFormats().joinToString(", ")}")
            return
        }

        val snapshot = route.waypoints.deepCopy()
        val minecraft = getMinecraft()

        BackgroundTasks.launchIO("export-waypoints") {
            val encoded = provider.export(snapshot)
            minecraft.execute {
                minecraft.keyboardHandler.clipboard = encoded
                ChatAPI.chat("Route was copied to clipboard.")
            }
        }
    }

    /** Persists an independent snapshot, initializing the route map even before joining Hypixel. */
    fun save(name: String) {
        if (MiningRouteIds.isReserved(name)) {
            ChatAPI.userError("The eurybium: namespace is reserved for repository routes.")
            return
        }

        if (name.isBlank()) {
            ChatAPI.userError("Route name must not be blank.")
            return
        }

        val routes = EurybiumMod.orderedWaypointsRoutes.routes
            ?: mutableMapOf<String, Waypoints<EurybiumWaypoint>>().also { EurybiumMod.orderedWaypointsRoutes.routes = it }

        routes[name] = route.waypoints.deepCopy()
        saveRoutes()

        ChatAPI.chat("Route saved as '$name'. Use /eybo load to select it.")
    }

    /** Deletes a saved route without unloading the active copy. */
    fun erase(name: String) {
        if (MiningRouteIds.isReserved(name)) {
            ChatAPI.userError("Repository routes cannot be erased. Rename legacy saved routes outside eurybium: first.")
            return
        }

        if (EurybiumMod.orderedWaypointsRoutes.routes?.remove(name) == null) {
            ChatAPI.userError("Route '$name' does not exist.")
            return
        }

        saveRoutes()
        ChatAPI.chat("Route '$name' deleted.")
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
