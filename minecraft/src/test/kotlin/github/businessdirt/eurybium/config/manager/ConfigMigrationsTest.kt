package github.businessdirt.eurybium.config.manager

import com.google.gson.JsonParser
import github.businessdirt.eurybium.config.EurybiumConfig
import github.businessdirt.eurybium.config.features.waypoints.OrderedWaypointsConfig
import github.businessdirt.eurybium.core.json.BaseGsonBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ConfigMigrationsTest {

    @Test
    fun `legacy waypoint settings move intact and leave mining settings alone`() {
        val json = JsonParser.parseString("""{"mining":{"pickaxeAbilityNotification":true,"orderedWaypoints":{"enabled":false,"renderMode":"FILL","waypointRange":7}}}""").asJsonObject
        val legacy = json.getAsJsonObject("mining").get("orderedWaypoints").deepCopy()

        ConfigMigrations.moveOrderedWaypoints(json)

        assertEquals(legacy, json.get("orderedWaypoints"))
        assertEquals(true, json.getAsJsonObject("mining").get("pickaxeAbilityNotification").asBoolean)
        assertFalse(json.getAsJsonObject("mining").has("orderedWaypoints"))
    }

    @Test
    fun `migrated config loads and saves the new category with default missing settings`() {
        val json = JsonParser.parseString("""{"mining":{"pickaxeAbilityNotification":true,"orderedWaypoints":{"enabled":false,"renderMode":"FILL","waypointRange":7}}}""")
        val gson = BaseGsonBuilder.lenientGson().create()
        ConfigMigrations.moveOrderedWaypoints(json)

        val config = gson.fromJson(json, EurybiumConfig::class.java)

        assertFalse(config.orderedWaypoints.enabled)
        assertEquals(OrderedWaypointsConfig.RenderMode.FILL, config.orderedWaypoints.renderMode)
        assertEquals(7f, config.orderedWaypoints.waypointRange)
        assertEquals(true, config.orderedWaypoints.traceLine)
        assertEquals(true, config.mining.pickaxeAbilityNotification)
        val saved = gson.toJsonTree(config).asJsonObject
        assertFalse(saved.getAsJsonObject("mining").has("orderedWaypoints"))
        // Accordion expansion state belongs to the UI, not the saved settings.
        assertFalse(saved.getAsJsonObject("orderedWaypoints").has("rendering"))
    }

    @Test
    fun `new category takes precedence and migration is idempotent`() {
        val json = JsonParser.parseString("""{"mining":{"orderedWaypoints":{"enabled":false}},"orderedWaypoints":{"enabled":true}}""").asJsonObject

        ConfigMigrations.moveOrderedWaypoints(json)
        val migrated = json.deepCopy()
        ConfigMigrations.moveOrderedWaypoints(json)

        assertEquals(true, json.getAsJsonObject("orderedWaypoints").get("enabled").asBoolean)
        assertEquals(migrated, json)
    }

    @Test
    fun `null new category can recover legacy settings`() {
        val json = JsonParser.parseString("""{"mining":{"orderedWaypoints":{"renderMode":"GLOW"}},"orderedWaypoints":null}""").asJsonObject

        ConfigMigrations.moveOrderedWaypoints(json)

        assertEquals("GLOW", json.getAsJsonObject("orderedWaypoints").get("renderMode").asString)
    }

    @Test
    fun `configs without legacy settings are unchanged`() {
        for (text in listOf("null", "[]", "{}", "{\"mining\":null}", "{\"mining\":{}}")) {
            val json = JsonParser.parseString(text)
            val original = json.deepCopy()
            ConfigMigrations.moveOrderedWaypoints(json)
            assertEquals(original, json)
        }
        ConfigMigrations.moveOrderedWaypoints(null)
    }
}
