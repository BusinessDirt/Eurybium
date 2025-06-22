package github.businessdirt.eurybium.config.features.gui

import gg.essential.vigilance.Vigilant.CategoryPropertyBuilder
import github.businessdirt.eurybium.config.ConfigCategory

class GuiConfig : ConfigCategory() {
    var timeFormat24h: Boolean = true

    override fun invoke(builder: CategoryPropertyBuilder) {
        builder.checkbox(
            ::timeFormat24h,
            "Time Format",
            "Change Eurybium to use 24h time instead of 12h time."
        )
    }
}
