package github.businessdirt.eurybium.config.features

import gg.essential.vigilance.Vigilant.CategoryPropertyBuilder
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.config.ConfigCategory

class About : ConfigCategory() {
    val licenses: Licenses = Licenses()

    override fun invoke(builder: CategoryPropertyBuilder) {
        builder.subcategory("Licenses", licenses)
    }

    class Licenses : ConfigCategory() {

        override fun invoke(builder: CategoryPropertyBuilder) {
            builder.button("Mixin", "Mixin is available under the MIT License", "Source") {
                // TODO: open browser
            }
        }
    }
}
