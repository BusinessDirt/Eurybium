package github.businessdirt.eurybium.config.features.mining.glacite

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MineshaftConfig {

    @Expose
    @ConfigOption(
        name = "Mineshaft Detection",
        desc = "Detects when you enter a mineshaft and displays the type of mineshaft you entered."
    )
    @ConfigEditorBoolean
    var detectMineshaft: Boolean = false

    @Expose
    @ConfigOption(name = "Mineshaft Mining", desc = "Features related to Mineshaft Mining.")
    @Accordion
    var mineshaftMining: MineshaftMining = MineshaftMining()
}
