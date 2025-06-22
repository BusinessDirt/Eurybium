package github.businessdirt.eurybium.config.features.mining

import gg.essential.vigilance.Vigilant.CategoryPropertyBuilder
import github.businessdirt.eurybium.config.ConfigCategory
import github.businessdirt.eurybium.config.features.mining.glacite.MineshaftConfig

class MiningConfig : ConfigCategory() {
    val glaciteMineshaft: MineshaftConfig = MineshaftConfig()

    override fun invoke(builder: CategoryPropertyBuilder) {
        builder.subcategory("Glacite Mineshaft", glaciteMineshaft)
    }
}
