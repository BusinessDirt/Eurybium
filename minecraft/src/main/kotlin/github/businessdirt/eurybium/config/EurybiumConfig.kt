package github.businessdirt.eurybium.config

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.config.features.About
import github.businessdirt.eurybium.config.features.dev.DevConfig
import github.businessdirt.eurybium.config.features.gui.GuiConfig
import github.businessdirt.eurybium.config.features.mining.MiningConfig
import github.businessdirt.eurybium.config.features.waypoints.OrderedWaypointsConfig
import github.businessdirt.eurybium.config.manager.ConfigFileType
import github.businessdirt.eurybium.generated.BuildInfo
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

class EurybiumConfig : Config() {

    override fun saveNow() {
        super.saveNow()
        EurybiumMod.configManager.saveConfig(ConfigFileType.CONFIG, "close-gui")
    }

    override fun getTitle(): StructuredText = StructuredText.of("${BuildInfo.NAME} - ${BuildInfo.VERSION}")

    @Category(name = "About", desc = "Information about Eurybium")
    @Expose var about: About = About()

    @Category(name = "GUI", desc = "Settings for GUI elements")
    @Expose var gui: GuiConfig = GuiConfig()

    @Category(name = "Mining", desc = "Features for the mining skill")
    @Expose var mining: MiningConfig = MiningConfig()

    @Category(name = "Ordered Waypoints", desc = "Ordered routes and waypoint rendering in any world")
    @Expose var orderedWaypoints: OrderedWaypointsConfig = OrderedWaypointsConfig()

    @Category(name = "Dev", desc = "Developer debug and test tools")
    @Expose var dev: DevConfig = DevConfig()
}
