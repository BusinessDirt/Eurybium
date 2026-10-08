package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.config.features.mining.glacitemineshaft.MineshaftWaypointsConfig.SpawningRoute.CUSTOM
import github.businessdirt.eurybium.config.features.mining.glacitemineshaft.MineshaftWaypointsConfig.SpawningRoute.NONE
import github.businessdirt.eurybium.core.utils.text.StringExtensions.ifNotNullOrEmpty
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.waypoints.MiningRouteIds.internalRouteId
import github.businessdirt.eurybium.events.skyblock.GlaciteMineshaftDetectionEvent
import github.businessdirt.eurybium.events.skyblock.IslandJoinEvent
import github.businessdirt.eurybium.features.waypoints.OrderedWaypoints
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object GlaciteMineshaftWaypoints {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.waypoints

    @HandleEvent
    private fun onGlaciteMineshaftDetectionEvent(event: GlaciteMineshaftDetectionEvent) {
        if(!config.autoLoadMineshaft) return

        OrderedWaypoints.load(name = event.type.internalRouteId, sendErrors = false)
    }

    @HandleEvent
    private fun onIslandJoinEvent(event: IslandJoinEvent) {
        if(config.spawningRoute == NONE) return
        if(event.island != IslandType.DWARVEN_MINES) return

        when (config.spawningRoute) {
            CUSTOM -> config.customSpawningRoute.ifNotNullOrEmpty { OrderedWaypoints.loadWaypoints(data = it) }
            else -> config.spawningRoute.routeId.ifNotNullOrEmpty { OrderedWaypoints.load(name = it, sendErrors = false) }
        }
    }
}
