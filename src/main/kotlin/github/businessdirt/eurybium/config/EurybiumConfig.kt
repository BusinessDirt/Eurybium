package github.businessdirt.eurybium.config

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.config.features.About
import github.businessdirt.eurybium.config.features.dev.DevConfig
import github.businessdirt.eurybium.config.features.gui.GuiConfig
import github.businessdirt.eurybium.config.features.mining.MiningConfig
import github.businessdirt.eurybium.config.manager.ConfigFileType
import github.businessdirt.eurybium.utils.Reference
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

class EurybiumConfig : Config() {

    override fun saveNow() {
        super.saveNow()
        EurybiumMod.configManager.saveConfig(ConfigFileType.CONFIG, "close-gui")
    }

    override fun getTitle(): StructuredText =
        StructuredText.of("${Reference.MOD_NAME} ${Reference.VERSION}")

    @Expose
    @Category(name = "About", desc = "Information about ${Reference.MOD_NAME}")
    var about: About = About()

    @Expose
    @Category(name = "GUI", desc = "Settings for GUI elements")
    var gui: GuiConfig = GuiConfig()

    @Expose
    @Category(name = "Mining", desc = "Features for the mining skill")
    var mining: MiningConfig = MiningConfig()

    @Expose
    @Category(name = "Dev", desc = "Developer debug and test tools")
    var dev: DevConfig = DevConfig()
}
