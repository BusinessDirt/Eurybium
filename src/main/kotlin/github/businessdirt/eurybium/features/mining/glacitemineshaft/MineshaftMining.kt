package github.businessdirt.eurybium.features.mining.glacitemineshaft

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.core.modules.EurybiumModule
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.events.hypixel.MineshaftEnteredEvent
import github.businessdirt.eurybium.events.hypixel.ScoreboardAreaChangedEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.features.types.GemstoneType
import github.businessdirt.eurybium.features.types.MineshaftType
import github.businessdirt.eurybium.utils.StringUtils.removeColor

@EurybiumModule
object MineshaftMining {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.mineshaftMining

    private var mineshaftType: MineshaftType = MineshaftType.UNKNOWN
    private var lapisCorpseCount = 0
    private var gotChecked = false

    @HandleEvent
    fun onTabWidgetUpdateEvent(event: TabWidgetUpdateEvent) {
        if (!config.detectViableMineshafts || mineshaftType == MineshaftType.UNKNOWN) return

        if (event.widget == TabWidget.FROZEN_CORPSES) {
            lapisCorpseCount = 0
            event.widget.lines.map { line ->
                if (line.removeColor().contains("Lapis"))
                    lapisCorpseCount++
            }

            EurybiumMod.logger.debug("Found $lapisCorpseCount Lapis corpses.")
        }
    }

    fun loadMineshaftWaypoints() {
        if (config.autoLoadWaypoints && mineshaftType.isGemstone) {
            val name = if (mineshaftType.name.endsWith("2")) "CRYSTAL"
            else mineshaftType.name

            if (EurybiumMod.orderedWaypointsRoutes.routes?.keys?.contains(name) == true ) {
                getMinecraft().player?.networkHandler?.sendCommand("eybo load $name")
            }
        }
    }

    @HandleEvent
    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {
        mineshaftType = event.type
        loadMineshaftWaypoints()
    }

    @HandleEvent
    fun onScoreboardAreaChangedEvent(event: ScoreboardAreaChangedEvent) {
        if (event.currentArea == event.previousArea) return
        if (event.currentArea.contains("Dwarven Base Camp") && config.autoLoadSpawnMineshaft != "") {
            getMinecraft().player?.networkHandler?.sendCommand("eybo load ${config.autoLoadSpawnMineshaft}")
        }
    }

    @HandleEvent
    fun onWorldChangeEvent(event: WorldChangeEvent) {
        mineshaftType = MineshaftType.UNKNOWN
    }

    @HandleEvent(onlyOnIsland = IslandType.MINESHAFT)
    fun onSecondPassedEvent(event: SecondPassedEvent) {
        if (mineshaftType == MineshaftType.UNKNOWN || gotChecked) return
        if (!mineshaftType.isGemstone) {
            EurybiumMod.logger.info("Not a gemstone mineshaft. Loot the Lapis corpses and §cLEAVE§e.")
            return
        }

        // TODO: fiesta
        val isViable = when(GemstoneType.fromMineshaftType(mineshaftType)) {
            GemstoneType.RUBY -> lapisCorpseCount >= config.corpseThresholds.ruby
            GemstoneType.AMBER -> lapisCorpseCount >= config.corpseThresholds.amber
            GemstoneType.SAPPHIRE -> lapisCorpseCount >= config.corpseThresholds.sapphire
            GemstoneType.JADE -> lapisCorpseCount >= config.corpseThresholds.jade
            GemstoneType.AMETHYST -> lapisCorpseCount >= config.corpseThresholds.amethyst
            GemstoneType.OPAL -> lapisCorpseCount >= config.corpseThresholds.opal
            GemstoneType.TOPAZ -> lapisCorpseCount >= config.corpseThresholds.topaz
            GemstoneType.JASPER -> lapisCorpseCount >= config.corpseThresholds.jasper
            GemstoneType.ONYX -> lapisCorpseCount >= config.corpseThresholds.onyx
            GemstoneType.AQUAMARINE -> lapisCorpseCount >= config.corpseThresholds.aquamarine
            GemstoneType.CITRINE -> lapisCorpseCount >= config.corpseThresholds.citrine
            GemstoneType.PERIDOT -> lapisCorpseCount >= config.corpseThresholds.peridot
            else -> throw RuntimeException("This should never have happened. mineshaft=$mineshaftType, gemstone=${GemstoneType.fromMineshaftType(mineshaftType)}")
        }

        var template = "This mineshaft has $lapisCorpseCount Lapis corpses. Loot them and "
        template += if (isViable) "§cMINE§e the gemstones." else "§cLEAVE§e."
        EurybiumMod.logger.info(template)

        gotChecked = true
    }
}