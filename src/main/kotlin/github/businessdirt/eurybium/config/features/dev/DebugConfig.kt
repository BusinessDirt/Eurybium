package github.businessdirt.eurybium.config.features.dev

import gg.essential.vigilance.Vigilant.CategoryPropertyBuilder
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import github.businessdirt.eurybium.config.ConfigCategory

class DebugConfig : ConfigCategory() {

    var enabled: Boolean = true

    override fun invoke(builder: CategoryPropertyBuilder) {
        builder.checkbox(::enabled, "Enable Debug", "Enable Test logic")
    }
}
