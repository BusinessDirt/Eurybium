package github.businessdirt.eurybium.config.features.dev

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DevConfig {

    @Accordion
    @ConfigOption(name = "Debug", desc = "")
    @Expose var debug: DebugConfig = DebugConfig()

    @ConfigEditorBoolean
    @ConfigOption(name = "Dev Commands", desc = "Enables Dev commands")
    @Expose var devCommands: Boolean = false
}
