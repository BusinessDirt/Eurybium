package github.businessdirt.eurybium.config.features.mining

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.config.features.mining.glacite.MineshaftConfig
import io.github.notenoughupdates.moulconfig.annotations.Category

class MiningConfig {

    @Expose
    @Category(name = "Glacite Mineshaft", desc = "Settings for the Glacite Mineshaft.")
    var glaciteMineshaft: MineshaftConfig = MineshaftConfig()
}
