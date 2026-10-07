package github.businessdirt.eurybium.data.model.waypoints

import github.businessdirt.eurybium.config.features.mining.glacitemineshaft.MineshaftWaypointsConfig.SpawningRoute
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.data.model.waypoints.MiningRouteIds.internalRouteId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiningRouteIdsTest {

    @Test
    fun `every shaft variant has a unique reserved route ID`() {
        val ids = MineshaftType.entries.map { it.internalRouteId }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all(MiningRouteIds::isReserved))

        assertFalse(MiningRouteIds.SHAFT_SPAWN_MITHRIL in ids)
        assertFalse(MiningRouteIds.SHAFT_SPAWN_TUNGSTEN in ids)
        assertFalse(MiningRouteIds.SHAFT_SPAWN_GEMSTONES in ids)

        assertEquals("eurybium:JASP1", MineshaftType.JASP_1.internalRouteId)
        assertEquals("eurybium:JASPC", MineshaftType.JASP_C.internalRouteId)
        assertEquals("eurybium:RUBYC", MineshaftType.RUBY_C.internalRouteId)

        assertEquals("eurybium:SHAFT_SPAWN_MITHRIL", SpawningRoute.MITHRIL.routeId)
        assertEquals("eurybium:SHAFT_SPAWN_TUNGSTEN", SpawningRoute.TUNGSTEN.routeId)
        assertEquals("eurybium:SHAFT_SPAWN_GEMSTONES", SpawningRoute.GEMSTONES.routeId)
    }

    @Test
    fun `reserved namespace cannot be bypassed through casing`() {
        assertTrue(MiningRouteIds.isReserved("EURYBIUM:JASP1"))
        assertFalse(MiningRouteIds.isReserved("my-jasper-route"))
        assertFalse(MiningRouteIds.isReserved("eurybium-route"))
    }
}
