package github.businessdirt.eurybium.features.mining.waypoints

import github.businessdirt.eurybium.data.model.waypoints.EurybiumWaypoint
import github.businessdirt.eurybium.data.model.waypoints.Waypoints
import github.businessdirt.eurybium.features.waypoints.OrderedWaypointRoute
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class OrderedWaypointRouteTest {

    private fun route(vararg positions: Int): OrderedWaypointRoute = OrderedWaypointRoute().apply {
        load(Waypoints(positions.mapIndexed { index, x -> EurybiumWaypoint(BlockPos(x, 0, 0), index + 1) }.toMutableList()))
    }

    private fun assertConsistent(route: OrderedWaypointRoute) {
        assertEquals((1..route.waypoints.size).toList(), route.waypoints.map { it.number })
        assertEquals(route.waypoints.map { it.number.toString() }, route.waypoints.map { it.options["name"] })
        assertTrue(route.visibleIndices().all { it in route.waypoints.indices })
        if (route.waypoints.isEmpty()) assertEquals(0, route.currentIndex)
        else assertTrue(route.currentIndex in route.waypoints.indices)
    }

    @Test
    fun `loading sorts and normalizes a private copy of sparse imported numbers`() {
        val source = Waypoints(mutableListOf(
            EurybiumWaypoint(BlockPos(30, 0, 0), 30, mutableMapOf("name" to "30")),
            EurybiumWaypoint(BlockPos(10, 0, 0), 10, mutableMapOf("name" to "10")),
            EurybiumWaypoint(BlockPos(20, 0, 0), 20, mutableMapOf("name" to "20")),
        ))
        val route = OrderedWaypointRoute().apply { load(source) }
        assertEquals(listOf(10, 20, 30), route.waypoints.map { it.location.x })
        assertConsistent(route)
        assertEquals(listOf(30, 10, 20), source.map { it.number })
        assertNotSame(source[1].options, route.waypoints[0].options)
        route.waypoints[0].options["custom"] = "changed"
        assertFalse(source[1].options.containsKey("custom"))
    }

    @Test
    fun `empty route navigation and selection never divide by zero`() {
        val route = route()
        assertFalse(route.move(1))
        assertFalse(route.skipTo(1))
        assertFalse(route.delete(1))
        assertTrue(route.visibleIndices().isEmpty())
        route.advanceIfNear(Vec3.ZERO, 3.0)
        assertConsistent(route)
    }

    @Test
    fun `large signed skips wrap without overflowing`() {
        val route = route(0, 10, 20)
        for (amount in listOf(Int.MAX_VALUE.toLong(), Int.MIN_VALUE.toLong(), Long.MAX_VALUE, Long.MIN_VALUE)) {
            val expected = Math.floorMod(route.currentIndex.toLong() + amount % 3, 3L).toInt()
            assertTrue(route.move(amount))
            assertEquals(expected, route.currentIndex)
            assertConsistent(route)
        }
        route.move(-1)
        assertConsistent(route)
    }

    @Test
    fun `inserting before current retains the current physical waypoint`() {
        val route = route(0, 10, 20)
        route.skipTo(2)
        assertTrue(route.add(1, BlockPos(5, 0, 0)))
        assertEquals(2, route.currentIndex)
        assertEquals(10, route.waypoints[route.currentIndex].location.x)
        assertTrue(route.add(5, BlockPos(40, 0, 0)))
        assertEquals(2, route.currentIndex)
        assertConsistent(route)
    }

    @Test
    fun `deleting before current retains it and deleting current selects its successor`() {
        val route = route(0, 10, 20, 30)
        route.skipTo(3)
        route.delete(1)
        assertEquals(1, route.currentIndex)
        assertEquals(20, route.waypoints[route.currentIndex].location.x)
        route.delete(2)
        assertEquals(30, route.waypoints[route.currentIndex].location.x)
        route.delete(2)
        assertEquals(0, route.currentIndex)
        assertEquals(10, route.waypoints.single().location.x)
        route.delete(1)
        assertConsistent(route)
    }

    @Test
    fun `invalid positions leave the route unchanged`() {
        val route = route(0, 10)
        for (number in listOf(Int.MIN_VALUE, -1, 0, 4, Int.MAX_VALUE)) {
            assertFalse(route.skipTo(number))
            assertFalse(route.delete(number))
            assertFalse(route.add(number, BlockPos.ZERO))
        }
        assertEquals(listOf(0, 10), route.waypoints.map { it.location.x })
        assertConsistent(route)
    }

    @Test
    fun `visible waypoint indexes deduplicate short routes and wrap longer routes`() {
        assertEquals(listOf(0), route(0).visibleIndices())
        assertEquals(listOf(1, 0), route(0, 10).visibleIndices())
        val route = route(0, 10, 20)
        assertEquals(listOf(2, 0, 1), route.visibleIndices())
        route.skipTo(3)
        assertEquals(listOf(1, 2, 0), route.visibleIndices())
    }

    @Test
    fun `nearby two waypoint routes do not alternate every frame`() {
        val route = route(0, 1)
        repeat(20) { route.advanceIfNear(Vec3(0.9, 0.0, 0.0), 3.0) }
        assertEquals(1, route.currentIndex)
    }

    @Test
    fun `automatic advancement requires range and proximity and wraps at the last point`() {
        val route = route(0, 10, 20)
        route.advanceIfNear(Vec3(9.0, 0.0, 0.0), 1.0)
        assertEquals(0, route.currentIndex)
        route.advanceIfNear(Vec3(9.5, 0.0, 0.0), 1.0)
        assertEquals(1, route.currentIndex)
        route.skipTo(3)
        route.advanceIfNear(Vec3.ZERO, 1.0)
        assertEquals(0, route.currentIndex)
    }

    @Test
    fun `invalid range or absent usable position cannot advance a route`() {
        val route = route(0, 1)
        for (range in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            route.advanceIfNear(Vec3(1.0, 0.0, 0.0), range)
            assertEquals(0, route.currentIndex)
        }
        route.advanceIfNear(Vec3(Double.NaN, 0.0, 0.0), 3.0)
        assertEquals(0, route.currentIndex)
    }

    @Test
    fun `clear resets selection and a new route does not inherit navigation state`() {
        val route = route(0, 10)
        route.skipTo(2)
        route.clear()
        assertConsistent(route)
        route.add(1, BlockPos.ZERO)
        assertEquals(0, route.currentIndex)
        assertConsistent(route)
    }
}
