package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.skyblock.MiningAPI
import github.businessdirt.eurybium.config.features.mining.glacitemineshaft.MineshaftWaypointsConfig.SpawningRoute.CUSTOM
import github.businessdirt.eurybium.config.features.mining.glacitemineshaft.MineshaftWaypointsConfig.SpawningRoute.NONE
import github.businessdirt.eurybium.core.utils.text.StringExtensions.ifNotNullOrEmpty
import github.businessdirt.eurybium.data.model.waypoints.MiningRouteIds.internalRouteId
import github.businessdirt.eurybium.events.skyblock.GlaciteMineshaftDetectionEvent
import github.businessdirt.eurybium.events.skyblock.SkyblockAreaChangeEvent
import github.businessdirt.eurybium.features.waypoints.OrderedWaypoints
import github.businessdirt.eurybium.processors.EurybiumModule

@EurybiumModule
object GlaciteMineshaftWaypoints {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.waypoints
    private var previouslyInGlaciteTunnels = false

    @HandleEvent
    private fun onGlaciteMineshaftDetectionEvent(event: GlaciteMineshaftDetectionEvent) {
        if(!config.autoLoadMineshaft) return

        OrderedWaypoints.load(name = event.type.internalRouteId, sendErrors = false)
    }

    @HandleEvent(events = [ SkyblockAreaChangeEvent::class ])
    private fun onSkyblockAreaChangeEvent() {
        if(config.spawningRoute == NONE) return

        if (previouslyInGlaciteTunnels && MiningAPI.inGlaciteTunnels) return

        if(!MiningAPI.inGlaciteTunnels) {
            previouslyInGlaciteTunnels = false
            return
        }

        previouslyInGlaciteTunnels = true

        when (config.spawningRoute) {
            CUSTOM -> config.customSpawningRoute.ifNotNullOrEmpty { OrderedWaypoints.loadWaypoints(data = it) }
            else -> config.spawningRoute.routeId.ifNotNullOrEmpty { OrderedWaypoints.load(name = it, sendErrors = false) }
        }
    }
}
