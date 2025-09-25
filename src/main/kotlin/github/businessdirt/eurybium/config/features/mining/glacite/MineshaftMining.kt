package github.businessdirt.eurybium.config.features.mining.glacite

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.*

class MineshaftMining {

    @Transient
    @ConfigOption(name = "Description", desc = "These features are related to the Mineshaft Mining as described in the Mining Cult Discord server. §eRequires 'Mineshaft Detection' to be enabled.")
    @ConfigEditorInfoText
    var extensiveDescription: Unit? = null

    @Expose
    @ConfigOption(name = "Detect Viable Mineshaft", desc = "Detects if a mineshaft has enough corpses for it to be 'profitable'.")
    @ConfigEditorBoolean
    var detectViableMineshafts: Boolean = false

    @Expose
    @ConfigOption(name = "Auto load Waypoints", desc = "Automatically loads waypoints when entering a mineshaft.")
    @ConfigEditorBoolean
    var autoLoadWaypoints: Boolean = false

    @Transient
    @ConfigOption(name = "Thresholds", desc = "Set the minimum amount of corpses needed for the mineshaft to be considered viable (also for fiestas). Set the value to 5 to 'disable' the shaft.")
    @ConfigEditorInfoText()
    var thresholdDescription: Unit? = null

    @Expose
    @ConfigOption(name = "Corpse Thresholds", desc = "")
    @Accordion
    var corpseThresholds: CorpseThresholds = CorpseThresholds(4f, 3f, 3f, 3f, 2f, 2f, 4f, 0f, 5f, 5f, 5f, 3f)

    @Expose
    @ConfigOption(name = "Fiesta Corpse Thresholds", desc = "")
    @Accordion
    var fiestaCorpseThresholds: CorpseThresholds = CorpseThresholds(4f, 1f, 1f, 2f, 1f, 5f, 3f, 0f, 5f, 5f, 5f, 5f)


    class CorpseThresholds(
        @Expose
        @ConfigOption(name = "Ruby", desc = "Threshold for Ruby Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f) var ruby: Float,

        @Expose
        @ConfigOption(name = "Amber", desc = "Threshold for Amber Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f) var amber: Float,
        @Expose
        @ConfigOption(name = "Sapphire", desc = "Threshold for Sapphire Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var sapphire: Float,

        @Expose
        @ConfigOption(name = "Jade", desc = "Threshold for Jade Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var jade: Float,

        @Expose
        @ConfigOption(name = "Amethyst", desc = "Threshold for Amethyst Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var amethyst: Float,

        @Expose
        @ConfigOption(name = "Opal", desc = "Threshold for Opal Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var opal: Float,

        @Expose
        @ConfigOption(name = "Topaz", desc = "Threshold for Topaz Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var topaz: Float,

        @Expose
        @ConfigOption(name = "Jasper", desc = "Threshold for Jasper Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var jasper: Float,

        @Expose
        @ConfigOption(name = "Onyx", desc = "Threshold for Onyx Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var onyx: Float,

        @Expose
        @ConfigOption(name = "Aquamarine", desc = "Threshold for Aquamarine Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var aquamarine: Float,

        @Expose
        @ConfigOption(name = "Citrine", desc = "Threshold for Citrine Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var citrine: Float,

        @Expose
        @ConfigOption(name = "Peridot", desc = "Threshold for Peridot Mineshafts")
        @ConfigEditorSlider(minStep = 1.0f, minValue = 0.0f, maxValue = 5.0f)
        var peridot: Float,
    )
}