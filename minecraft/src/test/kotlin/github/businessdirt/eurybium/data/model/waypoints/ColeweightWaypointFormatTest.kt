package github.businessdirt.eurybium.data.model.waypoints

import com.google.gson.JsonParser
import github.businessdirt.eurybium.config.manager.ConfigManager
import net.minecraft.core.BlockPos
import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ColeweightWaypointFormatTest {

    private val format = ColeweightWaypointFormat()
    private val valid = """[{"x":1,"y":2,"z":3,"r":0.0,"g":1.0,"b":0.0,"options":{"name":"7","custom":"value"}}]"""

    @Test
    fun `imports ordered Coleweight arrays and additional options`() {
        val route = assertNotNull(format.load(valid))
        assertEquals(BlockPos(1, 2, 3), route.single().location)
        assertEquals(7, route.single().number)
        assertEquals("value", route.single().options["custom"])
        assertTrue(format.canLoad(valid))
    }

    @Test
    fun `export round trip uses current number without modifying source options`() {
        val waypoint = EurybiumWaypoint(BlockPos(1, 2, 3), 8, mutableMapOf("name" to "7", "custom" to "value"))
        val exported = format.export(Waypoints(mutableListOf(waypoint)))
        assertTrue(JsonParser.parseString(exported).isJsonArray)
        val loaded = assertNotNull(format.load(exported)).single()
        assertEquals(8, loaded.number)
        assertEquals(waypoint.location, loaded.location)
        assertEquals("value", loaded.options["custom"])
        assertEquals("7", waypoint.options["name"])
        loaded.options["custom"] = "changed"
        assertEquals("value", waypoint.options["custom"])
    }

    @Test
    fun `new waypoints export an importable number without requiring name options`() {
        val waypoint = EurybiumWaypoint(BlockPos.ZERO, 3)
        val loaded = assertNotNull(format.load(format.export(Waypoints(mutableListOf(waypoint)))))
        assertEquals(3, loaded.single().number)
        assertTrue(waypoint.options.isEmpty())
    }

    @Test
    fun `invalid JSON fields entries or numbers reject the entire route`() {
        val invalid = listOf(
            "not JSON", "null", "{}", "[null]",
            valid.replace("\"x\":1,", ""),
            valid.replace("\"options\":{\"name\":\"7\",\"custom\":\"value\"}", "\"options\":null"),
            valid.replace("\"name\":\"7\",", ""),
            valid.replace("\"name\":\"7\"", "\"name\":\"text\""),
            valid.replace("\"name\":\"7\"", "\"name\":\"2147483648\""),
            valid.dropLast(1) + ",null]",
        )
        for (input in invalid) {
            assertNull(format.load(input), input)
            assertFalse(format.canLoad(input), input)
        }
    }

    @Test
    fun `empty routes are valid and round trip`() {
        assertTrue(assertNotNull(format.load("[]")).isEmpty())
        assertEquals("[]", format.export(Waypoints()))
    }

    @Test
    fun `list like route container keeps existing JSON array representation`() {
        val route = Waypoints(mutableListOf(ColeweightWaypoint(1, 2, 3, 0.0, 1.0, 0.0, mutableMapOf("name" to "1"))))
        val json = ConfigManager.gson.toJson(route, object : com.google.gson.reflect.TypeToken<Waypoints<ColeweightWaypoint>>() {}.type)
        assertTrue(JsonParser.parseString(json).isJsonArray)
        assertEquals(1, assertNotNull(format.load(json)).single().number)
    }

    @Test
    fun `AutoService registers a discoverable Coleweight provider`() {
        val providers = ServiceLoader.load(WaypointFormat::class.java, WaypointFormat::class.java.classLoader).toList()
        assertTrue(providers.any { it is ColeweightWaypointFormat && it.name == "coleweight" })
    }
}
