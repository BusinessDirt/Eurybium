package github.businessdirt.eurybium.config.features.mining.glacitemineshaft

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.*

/** Per-gemstone corpse thresholds for normal mining and Mining Fiesta. Five disables that type. */
class CorpseNotificationsConfig {

    @ConfigEditorBoolean
    @ConfigOption(name = "Enable Corpse Notifications", desc = "Notify once when the detected shaft reaches its corpse threshold. Mineshaft type announcements do not need to be enabled.")
    @Expose var enabled: Boolean = false

    @ConfigEditorInfoText
    @ConfigOption(name = "Thresholds", desc = "Set the minimum corpse count required for a notification. Use the Fiesta thresholds during Mining Fiesta. Five disables notifications for that gemstone.")
    @Transient var thresholdDescription: Unit? = null

    @Accordion
    @ConfigOption(name = "Corpse Thresholds", desc = "Minimum corpse counts outside Mining Fiesta.")
    @Expose var corpseThresholds: CorpseThresholds = CorpseThresholds(4f, 3f, 3f, 3f, 2f, 2f, 4f, 0f, 5f, 5f, 5f, 3f)

    @Accordion
    @ConfigOption(name = "Fiesta Corpse Thresholds", desc = "Minimum corpse counts during Mining Fiesta.")
    @Expose var fiestaCorpseThresholds: CorpseThresholds = CorpseThresholds(4f, 1f, 1f, 2f, 1f, 5f, 3f, 0f, 5f, 5f, 5f, 5f)

    class CorpseThresholds(
        @ConfigOption(name = "Ruby", desc = "Threshold for Ruby Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var ruby: Float,

        @ConfigOption(name = "Amber", desc = "Threshold for Amber Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var amber: Float,

        @ConfigOption(name = "Sapphire", desc = "Threshold for Sapphire Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var sapphire: Float,

        @ConfigOption(name = "Jade", desc = "Threshold for Jade Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var jade: Float,

        @ConfigOption(name = "Amethyst", desc = "Threshold for Amethyst Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var amethyst: Float,

        @ConfigOption(name = "Opal", desc = "Threshold for Opal Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var opal: Float,

        @ConfigOption(name = "Topaz", desc = "Threshold for Topaz Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var topaz: Float,

        @ConfigOption(name = "Jasper", desc = "Threshold for Jasper Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var jasper: Float,

        @ConfigOption(name = "Onyx", desc = "Threshold for Onyx Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var onyx: Float,

        @ConfigOption(name = "Aquamarine", desc = "Threshold for Aquamarine Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var aquamarine: Float,

        @ConfigOption(name = "Citrine", desc = "Threshold for Citrine Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var citrine: Float,

        @ConfigOption(name = "Peridot", desc = "Threshold for Peridot Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        @Expose var peridot: Float,
    )
}
