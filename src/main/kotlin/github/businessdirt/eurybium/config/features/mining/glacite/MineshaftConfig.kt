package github.businessdirt.eurybium.config.features.mining.glacite

import gg.essential.vigilance.Vigilant.CategoryPropertyBuilder
import github.businessdirt.eurybium.config.ConfigCategory

class MineshaftConfig : ConfigCategory() {

    var detectMineshaft: Boolean = false

    var gemstonePriceThreshold: Float = 13.0f
    var mineshaftCorpseMultiplier: Float = 1.1f

    override fun invoke(builder: CategoryPropertyBuilder) {
        builder.switch(
            ::detectMineshaft,
            "Mineshaft Detection",
            "Detects when you enter a mineshaft and displays the type of mineshaft you entered."
        )
        builder.decimalSlider(
            ::gemstonePriceThreshold,
            "Gemstone Price Threshold",
            "The minimum price per gemstone for mineshaft mining after normalizing. This is not related to any in-game price",
            1.0f, 100.0f, 1
        )
        builder.decimalSlider(
            ::gemstonePriceThreshold,
            "Mineshaft Corpse Multiplier",
            "The amount to multiply the bazaar price by per corpse.",
            1.0f, 2.0f, 1
        )
    }
}
