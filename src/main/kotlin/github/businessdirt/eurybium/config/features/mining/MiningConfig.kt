package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.config.features.mining.glacite.MineshaftConfig
import github.businessdirt.eurybium.features.mining.OrderedWaypoints
import io.github.notenoughupdates.moulconfig.annotations.Category

class MiningConfig {

    @Category(name = "Ordered Waypoints", desc = "")
    @Expose var orderedWaypoints: OrderedWaypointsConfig = OrderedWaypointsConfig()

    @Category(name = "Glacite Mineshaft", desc = "")
    @Expose var glaciteMineshaft: MineshaftConfig = MineshaftConfig()
}
