package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.config.features.mining.glacite.MineshaftConfig
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiningConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Pickaxe Ability Notification", desc = "Notification when the pickaxe ability cooldown is ready")
    @Expose var pickaxeAbilityNotification: Boolean = false

    @Category(name = "Ordered Waypoints", desc = "")
    @Expose var orderedWaypoints: OrderedWaypointsConfig = OrderedWaypointsConfig()

    @Category(name = "Glacite Mineshaft", desc = "")
    @Expose var glaciteMineshaft: MineshaftConfig = MineshaftConfig()
}
