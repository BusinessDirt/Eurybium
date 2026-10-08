package github.businessdirt.eurybium.data.model.waypoints

import net.minecraft.core.BlockPos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class WaypointModelsTest {

    @Test
    fun `waypoint snapshots a mutable block position`() {
        val position = BlockPos.MutableBlockPos(1, 2, 3)
        val waypoint = EurybiumWaypoint(position, 1)
        position.set(4, 5, 6)
        assertEquals(BlockPos(1, 2, 3), waypoint.location)
    }

    @Test
    fun `deep copy separates route waypoints options and material preference`() {
        val waypoint = EurybiumWaypoint(BlockPos(1, 2, 3), 1, mutableMapOf("custom" to "original"))
        waypoint.nodeMaterial = "minecraft:diamond_ore"
        val original = Waypoints(mutableListOf(waypoint))
        val copy = original.deepCopy()
        assertNotSame(original.waypoints, copy.waypoints)
        assertNotSame(waypoint, copy.single())
        assertNotSame(waypoint.options, copy.single().options)
        assertEquals("minecraft:diamond_ore", copy.single().nodeMaterial)
        copy.single().nodeMaterial = null
        assertNull(copy.single().nodeMaterial)
        copy.single().number = 2
        copy.single().options["custom"] = "changed"
        copy.clear()
        assertEquals(1, original.single().number)
        assertEquals("original", original.single().options["custom"])
        assertEquals("minecraft:diamond_ore", original.single().nodeMaterial)
    }

    @Test
    fun `material preference survives Coleweight export and import`() {
        val waypoint = EurybiumWaypoint(BlockPos(1, 2, 3), 1)
        waypoint.nodeMaterial = "  MINECRAFT:MAGENTA_STAINED_GLASS  "
        val format = ColeweightWaypointFormat()
        val restored = format.load(format.export(Waypoints(mutableListOf(waypoint))))!!.single()
        assertEquals("minecraft:magenta_stained_glass", restored.nodeMaterial)
        restored.nodeMaterial = " "
        assertNull(restored.nodeMaterial)
    }

    @Test
    fun `Coleweight copy has independent options`() {
        val original = ColeweightWaypoint(1, 2, 3, 0.0, 1.0, 0.0, mutableMapOf("name" to "1"))
        val copy = original.copy()
        copy.options["name"] = "2"
        assertEquals("1", original.options["name"])
    }

    @Test
    fun `delegated list edits and transforms preserve backing list and route order`() {
        val backing = mutableListOf(EurybiumWaypoint(BlockPos.ZERO, 1))
        val route = Waypoints(backing)
        route += EurybiumWaypoint(BlockPos(1, 2, 3), 2)
        assertEquals(2, backing.size)
        val transformed = route.transform { ColeweightWaypoint(it.location.x, it.location.y, it.location.z, 0.0, 1.0, 0.0) }
        assertEquals(listOf(0, 1), transformed.map { it.x })
        transformed.clear()
        assertEquals(listOf(1, 2), route.map { it.number })
    }
}
