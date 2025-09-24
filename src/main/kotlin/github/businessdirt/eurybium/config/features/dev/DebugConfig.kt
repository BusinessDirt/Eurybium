package github.businessdirt.eurybium.config.features.dev

import gg.essential.vigilance.Vigilant.CategoryPropertyBuilder
import github.businessdirt.eurybium.config.ConfigCategory

class DebugConfig : ConfigCategory() {

    var enabled: Boolean = true

    override fun invoke(builder: CategoryPropertyBuilder) {
        builder.checkbox(::enabled, "Enable Debug", "Enable Test logic")
    }
}
