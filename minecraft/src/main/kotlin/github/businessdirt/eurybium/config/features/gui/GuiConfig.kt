package github.businessdirt.eurybium.config.features.gui

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GuiConfig {

    @Expose
    @ConfigEditorBoolean
    @ConfigOption(name = "Time Format", desc = "Change Eurybium to use 24h time instead of 12h time.")
    var timeFormat24h: Boolean = true
}
