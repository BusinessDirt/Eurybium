package github.businessdirt.eurybium.config.features.dev

import gg.essential.vigilance.Vigilant.CategoryPropertyBuilder
import github.businessdirt.eurybium.config.ConfigCategory

class DevConfig : ConfigCategory() {
    val debug: DebugConfig = DebugConfig()

    override fun invoke(builder: CategoryPropertyBuilder) {
        builder.subcategory("Debug", debug)
    }
}
