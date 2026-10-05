package github.businessdirt.eurybium.config

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.config.features.About
import github.businessdirt.eurybium.config.features.dev.DevConfig
import github.businessdirt.eurybium.config.features.gui.GuiConfig
import github.businessdirt.eurybium.generated.BuildInfo
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

class EurybiumConfig : Config() {

    override fun getTitle(): StructuredText = StructuredText.of("Eurybium - ${BuildInfo.VERSION}")

    @Expose
    @Category(name = "About", desc = "Information about Eurybium")
    var about: About = About()

    @Expose
    @Category(name = "GUI", desc = "Settings for GUI elements")
    var gui: GuiConfig = GuiConfig()

    @Expose
    @Category(name = "Dev", desc = "Developer debug and test tools")
    var dev: DevConfig = DevConfig()
}
