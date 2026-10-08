package github.businessdirt.eurybium.data.repo

import github.businessdirt.eurybium.api.repo.RepoWaypointRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

class RepoRouteMaterialTest {
    private fun parse(point: String) = RepoParser.parse(REVISION_A, 1,
        repoFiles() + ("mining/routes.json" to JASPER_ROUTE.replace("[10,100,20]", point)))

    @Test
    fun `material is validated and preserved in editable route copies`() {
        val snapshot = parse("""[10,100,20,"minecraft:magenta_stained_glass"]""")
        val handle = RepoWaypointRoute("eurybium:JASP1")
        val waypoints = handle.waypoints(snapshot)!!
        assertEquals("minecraft:magenta_stained_glass", waypoints[0].nodeMaterial)
        assertNull(waypoints[1].nodeMaterial)
        waypoints[0].nodeMaterial = "mithril"
        assertEquals("minecraft:magenta_stained_glass", handle.waypoints(snapshot)!![0].nodeMaterial)
        assertEquals("minecraft:magenta_stained_glass", snapshot.routes.getValue(handle.id).points[0].nodeMaterial)
    }

    @Test
    fun `blank null and legacy points remain unrestricted`() {
        for (point in listOf("[10,100,20]", "[10,100,20,null]", "[10,100,20,\"\"]")) {
            assertNull(parse(point).routes.getValue("eurybium:JASP1").points[0].nodeMaterial)
        }
    }

    @Test
    fun `material names normalize and unknown or malformed values are rejected`() {
        assertEquals("minecraft:magenta_stained_glass", parse("""[10,100,20,"jasper"]""").routes.values.single().points[0].nodeMaterial)
        for (point in listOf("[10,100,20,4]", "[10,100,20,{}]", """[10,100,20,"nonsense"]""", """[10,100,20,"jasper",0]""")) {
            assertFails { parse(point) }
        }
    }
}
