package github.businessdirt.eurybium.config.features.dev

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DevConfig {

    @Expose
    @Accordion
    @ConfigOption(name = "Debug", desc = "")
    var debug: DebugConfig = DebugConfig()
}
