package github.businessdirt.eurybium.features.mining.waypoints

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.commands.commandSource
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.data.model.waypoints.Waypoints
import github.businessdirt.eurybium.features.waypoints.OrderedWaypoints
import github.businessdirt.eurybium.features.waypoints.OrderedWaypointsCommand
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OrderedWaypointsCommandTest {

    private fun dispatcher(): CommandDispatcher<FabricClientCommandSource> =
        CommandDispatcher<FabricClientCommandSource>().also {
            OrderedWaypointsCommand.onCommandRegistration(CommandRegistrationEvent(it))
        }

    @Test
    fun `primary command alias and subcommand aliases are registered`() {
        val dispatcher = dispatcher()
        for (name in listOf("eybordered", "eybo")) {
            val node = assertNotNull(dispatcher.root.getChild(name))
            val root = node.redirect ?: node
            for (child in listOf("load", "import", "unload", "clear", "delete", "remove", "add", "insert", "erase", "delete-route")) {
                assertNotNull(root.getChild(child), child)
            }
        }
    }

    @Test
    fun `zero and negative positions and counts are rejected before command callbacks`() {
        val dispatcher = dispatcher()
        for (command in listOf("skip", "unskip", "skipto", "add", "delete")) {
            for (number in listOf(0, -1, Int.MIN_VALUE)) {
                assertFailsWith<CommandSyntaxException> { dispatcher.execute("eybo $command $number", commandSource()) }
            }
        }
    }

    @Test
    fun `saved route suggestions refresh when routes change`() {
        val original = EurybiumMod.orderedWaypointsRoutes.routes
        try {
            EurybiumMod.orderedWaypointsRoutes.routes = mutableMapOf()
            val dispatcher = dispatcher()
            fun suggestions(): List<String> = dispatcher.getCompletionSuggestions(
                dispatcher.parse("eybo load ", commandSource()),
            ).join().list.map { it.text }
            assertTrue(suggestions().isEmpty())
            EurybiumMod.orderedWaypointsRoutes.routes!!["example"] = Waypoints()
            assertEquals(listOf("example"), suggestions())
        } finally {
            EurybiumMod.orderedWaypointsRoutes.routes = original
        }
    }

    @Test
    fun `registered formats import and export independently of system locale`() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertTrue("coleweight" in OrderedWaypoints.getWaypointFormats())
            val route = assertNotNull(OrderedWaypoints.loadWaypoints("[]"))
            assertEquals("[]", OrderedWaypoints.exportWaypoints(route, "COLEWEIGHT"))
        } finally {
            Locale.setDefault(original)
        }
    }
}
