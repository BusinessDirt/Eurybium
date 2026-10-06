package github.businessdirt.eurybium.config.features.dev

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DebugConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Enable Debug", desc = "Enable Test logic")
    @Expose var enabled: Boolean = false
}
