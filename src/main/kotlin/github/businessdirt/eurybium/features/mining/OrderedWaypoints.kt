package github.businessdirt.eurybium.features.mining

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.commands.CommandCategory
import github.businessdirt.eurybium.commands.SuggestionProviders
import github.businessdirt.eurybium.commands.brigadier.BrigadierArguments
import github.businessdirt.eurybium.config.features.mining.OrderedWaypointsConfig
import github.businessdirt.eurybium.config.manager.ConfigFileType
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.scanner.GemstoneNodeScanner
import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import github.businessdirt.eurybium.data.model.waypoints.WaypointFormat
import github.businessdirt.eurybium.data.model.waypoints.Waypoints
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.hypixel.HypixelJoinEvent
import github.businessdirt.eurybium.events.hypixel.MineshaftEnteredEvent
import github.businessdirt.eurybium.events.hypixel.ScoreboardAreaChangedEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderLastEvent
import github.businessdirt.eurybium.features.types.MineshaftType
import github.businessdirt.eurybium.utils.ClipboardUtils
import github.businessdirt.eurybium.utils.MathUtils.distanceSqToPlayer
import github.businessdirt.eurybium.utils.MathUtils.distanceToPlayer
import github.businessdirt.eurybium.utils.StringUtils
import github.businessdirt.eurybium.utils.concurrent.Coroutine
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.floor

object OrderedWaypoints {
    private val config get() = EurybiumMod.config.mining.orderedWaypoints

    private val scanner = GemstoneNodeScanner()

    private var currentOrderedWaypointIndex = 0
    private val renderWaypoints: MutableList<Int> = mutableListOf()

    private var orderedWaypointsList = Waypoints<EurybiumWaypoint>()
    private val mineshaftNodes get() = EurybiumMod.gemstoneNodes.mineshaftNodes

    private var mineshaftType = MineshaftType.UNKNOWN
    private var lastCloser = 0

    private fun saveGemstoneNodes() { EurybiumMod.configManager.saveConfig(ConfigFileType.GEMSTONE_NODES, "Save file") }
    private fun saveRoutes() { EurybiumMod.configManager.saveConfig(ConfigFileType.ROUTES, "Save file") }

    private fun updateGemstoneNodes() {
        Coroutine.launch("updateGemstoneNodes") {
            scanner.find()

            with(mineshaftNodes!!.getOrPut(mineshaftType.typeIndex) { mutableListOf() }) {
                clear()
                addAll(scanner.clusterBlocks())
                EurybiumMod.logger.debug("Found $size gemstone nodes!")
            }

            saveGemstoneNodes()
        }
    }
    
    @HandleEvent
    fun onHypixelJoinEvent(event: HypixelJoinEvent) {
        if (EurybiumMod.orderedWaypointsRoutes.routes == null) {
            EurybiumMod.orderedWaypointsRoutes.routes = mutableMapOf()
            saveRoutes()
        }

        if (EurybiumMod.gemstoneNodes.mineshaftNodes == null) {
            EurybiumMod.gemstoneNodes.mineshaftNodes = mutableMapOf()
            saveGemstoneNodes()
        }
    }

    @HandleEvent
    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {
        mineshaftType = event.type
        if (mineshaftType == MineshaftType.UNKNOWN) {
            EurybiumMod.logger.debug("Entered mineshaft but type is 'UNKNOWN'. in MineshaftWaypoints::onMineshaftEnteredEvent(event: MineshaftEnteredEvent)")
            return
        }

        if (mineshaftNodes!![mineshaftType.typeIndex]?.isEmpty() == true) {
            EurybiumMod.logger.debug("Entered mineshaft of type '$mineshaftType' but no cached gemstone nodes were found.")
            EurybiumMod.logger.debug("Launching Coroutine to find gemstone nodes...")
            updateGemstoneNodes()
        }
    }
    
    @HandleEvent
    fun onWorldRenderLastEvent(event: WorldRenderLastEvent) {
        if (!config.enabled) return;

        for (i in renderWaypoints.indices) {
            val waypointColor = when (i) {
                0 -> config.previousWaypointColor
                1 -> config.currentWaypointColor
                2 -> config.nextWaypointColor
                else -> config.nextWaypointColor
            }

            if (orderedWaypointsList.size <= renderWaypoints[i]) {
                EurybiumMod.logger.debug("${renderWaypoints[i]} $i")
                continue
            }

            val waypoint = orderedWaypointsList[renderWaypoints[i]]
            when (config.renderMode) {
                OrderedWaypointsConfig.RenderMode.FILL ->
                    event.drawWaypointFilled(waypoint, waypointColor, true)

                OrderedWaypointsConfig.RenderMode.OUTLINE ->
                    event.drawWaypointOutlined(waypoint, waypointColor, config.blockOutlineThickness.toInt(), false)

                OrderedWaypointsConfig.RenderMode.GLOW ->
                    event.drawWaypointGlowing(waypoint, waypointColor, mineshaftType)
            }
        }

        if (renderWaypoints.size <= 1) return decideWaypoints()

        val traceWaypoint = if (renderWaypoints.size == 2) orderedWaypointsList[renderWaypoints[0]]
        else orderedWaypointsList[renderWaypoints[2]]

        val traceLocation = if (config.renderMode == OrderedWaypointsConfig.RenderMode.GLOW && mineshaftType != MineshaftType.UNKNOWN)
            traceWaypoint.getNearestNode(mineshaftType)?.getCenterPos() ?: Vec3d.ZERO
        else traceWaypoint.location.toCenterPos()

        if (config.traceLine) event.drawLineToEye(traceLocation, config.traceLineColor, config.traceLineThickness.toInt(), depth = true)

        decideWaypoints()
    }
    
    @HandleEvent
    fun onWorldChangeEvent(event: WorldChangeEvent) {
        currentOrderedWaypointIndex = 0
        mineshaftType = MineshaftType.UNKNOWN
        unload(false)
    }
    
    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("eybordered") {
            description = "Ordered Waypoints commands."
            category = CommandCategory.USERS_ACTIVE
            aliases = mutableListOf("eybo")
            literal("load", "import") {
                description = "Loads ordered waypoints from your clipboard or config."
                arg("name", BrigadierArguments.string(), SuggestionProviders.dynamic { getRouteNames() }) { name ->
                    callback { load(getArg(name)) }
                }
                simpleCallback { load("") }
            }
            literal("unload", "clear") {
                description = "Unloads the current ordered waypoints."
                simpleCallback { unload() }
            }
            literal("skip") {
                description = "Skips the next waypoint."
                arg("amount", BrigadierArguments.int()) { amount ->
                    callback { skip(getArg(amount)) }
                }
                simpleCallback { skip(1) }
            }
            literal("skipto") {
                description = "Skips to the waypoint with the inputted number."
                arg("number", BrigadierArguments.int()) { number ->
                    callback { skipto(getArg(number)) }
                }
                simpleCallback { skipto(1) }
            }
            literal("unskip") {
                description = "Goes back by the number inputted many waypoints."
                arg("amount", BrigadierArguments.int()) { amount ->
                    callback { unskip(getArg(amount)) }
                }
                simpleCallback { unskip(1) }
            }
            literal("delete", "remove") {
                description = "Deletes the waypoint with the inputted number."
                arg("number", BrigadierArguments.int()) { number ->
                    callback { delete(getArg(number)) }
                }
            }
            literal("add", "insert") {
                description = "Inserts a waypoint with the specified numbering below the player."
                arg("number", BrigadierArguments.int()) { number ->
                    callback { add(getArg(number)) }
                }
            }
            literal("export") {
                description = "Exports the loaded ordered waypoints to clipboard."
                arg("format", BrigadierArguments.string(), SuggestionProviders.dynamic { getWaypointFormats() }) { format ->
                    callback { export(getArg(format)) }
                }
                simpleCallback { export("coleweight") }
            }
            literal("save") {
                description = "Saves the loaded ordered waypoints to your config."
                arg("name", BrigadierArguments.string()) { name ->
                    callback { save(getArg(name)) }
                }
            }
            literal("erase", "delete-route") {
                description = "Erases the route with the specified name."
                arg("name", BrigadierArguments.string(), SuggestionProviders.dynamic { getRouteNames() }) { name ->
                    callback { erase(getArg(name)) }
                }
            }
        }

        event.register("eurybiumforceloadgemstonenodes") {
            category = CommandCategory.DEVELOPER_TEST
            description = "Scans the world for gemstone blocks"
            simpleCallback { updateGemstoneNodes() }
        }
    }

    private fun getRouteNames() = EurybiumMod.orderedWaypointsRoutes.routes?.keys.orEmpty()

    private fun load(name: String) {
        Coroutine.launchIO("load") {
            val res = if (name == "") {
                loadWaypoints(ClipboardUtils.readFromClipboard().orEmpty())
            } else {
                val routes = EurybiumMod.orderedWaypointsRoutes.routes
                routes?.get(name) ?: run {
                    EurybiumMod.logger.error(
                        "Route $name doesn't exist.\n" +
                                "§cSaved Routes: ${routes?.keys?.toList()?.joinToString(", ")}\n" +
                                "§cIf you would like to import a route from your clipboard, leave the route name blank.",
                    )
                    return@launchIO
                }
            }

            res?.let {
                orderedWaypointsList = it.deepCopy()
                orderedWaypointsList.sortedBy { waypoint -> waypoint.number }
                currentOrderedWaypointIndex = orderedWaypointsList.minBy { waypoint -> waypoint.location.distanceSqToPlayer() }.number - 1
                renderWaypoints.clear()
                EurybiumMod.logger.info("Loaded ordered waypoints!")
            } ?: run {
                EurybiumMod.logger.error(
                    "There was an error parsing waypoints. " +
                            "Please make sure they are properly formatted and in a supported format.\n" +
                            "§cSupported Formats: ${getWaypointFormats().joinToString(", ")}",
                )
                return@launchIO
            }
        }
    }

    private fun unload(sendMessage: Boolean = true) {
        orderedWaypointsList.clear()
        renderWaypoints.clear()
        currentOrderedWaypointIndex = 0
        lastCloser = 0
        if (sendMessage) EurybiumMod.logger.info("Unloaded ordered waypoints.")
    }

    private fun skip(amount: Int) {
        if (orderedWaypointsList.isEmpty()) {
            return EurybiumMod.logger.error("There are no waypoints to skip.")
        }

        incrementIndex(amount)
        EurybiumMod.logger.info("Skipped $amount ${StringUtils.pluralize(amount, "waypoint")}.")
    }

    private fun skipto(number: Int) {
        if (orderedWaypointsList.isEmpty()) {
            return EurybiumMod.logger.info("There are no waypoints to skip to.")
        }

        val newOrderedWaypointIndex = number - 1
        if (0 <= newOrderedWaypointIndex && newOrderedWaypointIndex < orderedWaypointsList.size) {
            currentOrderedWaypointIndex = newOrderedWaypointIndex
            EurybiumMod.logger.info("Skipped to ${currentOrderedWaypointIndex + 1}.")
        } else {
            EurybiumMod.logger.error("$number is not between 1 and ${orderedWaypointsList.size}.")
        }
    }

    private fun unskip(amount: Int) {
        if (orderedWaypointsList.isEmpty()) {
            return EurybiumMod.logger.error("There are no waypoints to unskip.")
        }

        incrementIndex(-amount)

        EurybiumMod.logger.info("Unskipped $amount waypoints.")
    }

    private fun delete(number: Int) {
        if (orderedWaypointsList.isEmpty()) {
            return EurybiumMod.logger.error("There are no waypoints to delete.")
        }

        if (number < 1 || number > orderedWaypointsList.size) {
            return EurybiumMod.logger.error("$number is not between 1 and ${orderedWaypointsList.size}.")
        }

        for (i in number - 1 until orderedWaypointsList.size) {
            orderedWaypointsList[i].options["name"] = orderedWaypointsList[i].number.dec().toString()
            orderedWaypointsList[i].number--
        }
        orderedWaypointsList.removeAt(number - 1)
        renderWaypoints.clear()

        EurybiumMod.logger.info("Removed waypoint $number.")
    }

    private fun add(number: Int) {
        val playerPos = getMinecraft().player?.pos?.add(0.0, -1.0, 0.0)
        val pos = BlockPos(
            floor(playerPos?.x ?: 0.0).toInt(),
            floor(playerPos?.y ?: 0.0).toInt(),
            floor(playerPos?.z ?: 0.0).toInt()
        )

        if (number < 1 || number > orderedWaypointsList.size + 1) {
            return EurybiumMod.logger.error("$number is not between 1 and ${orderedWaypointsList.size + 1}.")
        }

        val newWaypoint = EurybiumWaypoint(pos, number = number, options = mutableMapOf("name" to number.toString()))
        if (number == orderedWaypointsList.size + 1) {
            orderedWaypointsList.add(newWaypoint)
        } else {
            for (i in number - 1 until orderedWaypointsList.size) {
                orderedWaypointsList[i].options["name"] = orderedWaypointsList[i].number.inc().toString()
                orderedWaypointsList[i].number++
            }
            orderedWaypointsList.add(number - 1, newWaypoint)
        }
        EurybiumMod.logger.info("Inserted waypoint $number at ${listOf(pos.x, pos.y, pos.z).joinToString(", ")}.")
    }

    private fun export(format: String) {
        Coroutine.launchIO("export") {
            val route = if (format.isEmpty()) exportWaypoints(orderedWaypointsList, "coleweight")
            else exportWaypoints(orderedWaypointsList, format.lowercase(Locale.getDefault()))

            route?.let {
                ClipboardUtils.copyToClipboard(it)
                EurybiumMod.logger.info("Route was copied to clipboard.")
            } ?: run {
                EurybiumMod.logger.error(
                    "Invalid waypoint format specified.\n" +
                            "§cFormats: ${getWaypointFormats().joinToString { ", " }}",
                )
            }
        }
    }

    private fun save(name: String) {
        EurybiumMod.orderedWaypointsRoutes.routes?.set(name, orderedWaypointsList.deepCopy())
        saveRoutes()
        EurybiumMod.logger.info("Route saved as $name. Do /eybo load $name to import it.")
    }

    private fun erase(name: String) {
        EurybiumMod.orderedWaypointsRoutes.routes?.remove(name) ?: run {
            EurybiumMod.logger.error("Route $name doesn't exist.")
            return
        }

        saveRoutes()
        EurybiumMod.logger.info("Route $name successfully deleted.")
    }

    private fun decideWaypoints() {
        renderWaypoints.clear()
        if (orderedWaypointsList.isEmpty()) return

        val beforeWaypoint = orderedWaypointsList.getOrNull(currentOrderedWaypointIndex - 1)
            ?: orderedWaypointsList.last()
        renderWaypoints.add(beforeWaypoint.number - 1)

        val currentWaypoint = orderedWaypointsList.getOrNull(currentOrderedWaypointIndex)

        var distanceToCurrent = Double.POSITIVE_INFINITY
        if (currentWaypoint != null) {
            distanceToCurrent = currentWaypoint.distanceToPlayer(config.renderMode, mineshaftType)
            renderWaypoints.add(currentWaypoint.number - 1)
        }

        val nextWaypoint = orderedWaypointsList.getOrNull(currentOrderedWaypointIndex + 1)
            ?: orderedWaypointsList.first()

        val distanceToNext = nextWaypoint.distanceToPlayer(config.renderMode, mineshaftType)
        if (nextWaypoint.number - 1 !in renderWaypoints) renderWaypoints.add(nextWaypoint.number - 1)

        val index = (nextWaypoint.number - 1) % orderedWaypointsList.size
        if (index !in renderWaypoints) renderWaypoints.add(index)

        if (
            lastCloser == currentOrderedWaypointIndex &&
            distanceToCurrent > distanceToNext &&
            distanceToNext < config.waypointRange
        ) {
            return incrementIndex(1)
        }

        if (distanceToCurrent < config.waypointRange.toDouble()) {
            lastCloser = currentOrderedWaypointIndex
        }

        if (distanceToNext < config.waypointRange.toDouble()) {
            incrementIndex(1)
        }
    }

    private fun incrementIndex(increment: Int) {
        currentOrderedWaypointIndex = Math.floorMod(currentOrderedWaypointIndex + increment, orderedWaypointsList.size)
    }

    private fun loadWaypoints(data: String): Waypoints<EurybiumWaypoint>? {
        return ServiceLoader.load(WaypointFormat::class.java).firstNotNullOfOrNull {
            it.load(data)
        }?.let {
            Waypoints(it.toMutableList())
        }
    }

    private fun exportWaypoints(waypoints: Waypoints<EurybiumWaypoint>, name: String): String? {
        return ServiceLoader.load(WaypointFormat::class.java).firstOrNull { it.name == name }?.export(waypoints)
    }

    private fun getWaypointFormats(): List<String> {
        return ServiceLoader.load(WaypointFormat::class.java).map { it.name }
    }
}