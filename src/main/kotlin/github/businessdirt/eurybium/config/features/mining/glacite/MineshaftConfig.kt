package github.businessdirt.eurybium.config.features.mining.glacite

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MineshaftConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Mineshaft Detection", desc = "Detects when you enter a mineshaft and displays the type of mineshaft you entered.")
    @Expose var detectMineshaft: Boolean = false

    @Accordion
    @ConfigOption(name = "Mineshaft Mining", desc = "Features related to Mineshaft Mining.")
    @Expose var mineshaftMining: MineshaftMiningConfig = MineshaftMiningConfig()
}
