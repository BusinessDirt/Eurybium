package github.businessdirt.eurybium.config.features.gui

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.utils.Reference
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class GuiConfig {

    @Expose
    @ConfigOption(name = "Time Format", desc = "Change ${Reference.MOD_NAME} to use 24h time instead of 12h time.")
    @ConfigEditorBoolean
    var timeFormat24h: Boolean = true
}
