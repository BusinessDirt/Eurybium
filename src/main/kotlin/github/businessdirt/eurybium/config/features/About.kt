package github.businessdirt.eurybium.config.features

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class About {

    @Expose
    @Accordion
    @ConfigOption(name = "Used Software", desc = "Information about used software and licenses.")
    val licenses: Licenses = Licenses()

    class Licenses {

        @ConfigOption(name = "MoulConfig", desc = "MoulConfig is available under the LGPL 3.0 License or later version")
        @ConfigEditorButton(buttonText = "Source")
        val moulconfig: Runnable = Runnable {
            //openBrowser("https://github.com/NotEnoughUpdates/MoulConfig")
        }

        @ConfigOption(name = "Mixin", desc = "Mixin is available under the MIT License")
        @ConfigEditorButton(buttonText = "Source")
        val mixin: Runnable = Runnable {
            //openBrowser("https://github.com/SpongePowered/Mixin/")
        }
    }
}
