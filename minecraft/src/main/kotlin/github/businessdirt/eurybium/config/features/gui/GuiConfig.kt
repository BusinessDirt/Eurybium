package github.businessdirt.eurybium.config.features.gui

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.generated.BuildInfo
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GuiConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Time Format", desc = "Change ${BuildInfo.NAME} to use 24h time instead of 12h time.")
    @Expose var timeFormat24h: Boolean = true
}
