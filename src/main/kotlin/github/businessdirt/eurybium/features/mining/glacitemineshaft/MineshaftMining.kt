package github.businessdirt.eurybium.features.mining.glacitemineshaft

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.hypixel.MineshaftEnteredEvent
import github.businessdirt.eurybium.events.hypixel.ScoreboardAreaChangedEvent
import github.businessdirt.eurybium.features.mining.OrderedWaypoints
import github.businessdirt.eurybium.features.types.MineshaftType

object MineshaftMining {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft.mineshaftMining
    private var mineshaftType: MineshaftType = MineshaftType.UNKNOWN

    @HandleEvent
    fun onTabWidgetUpdateEvent(event: TabWidgetUpdateEvent) {
        if (!config.detectViableMineshafts || mineshaftType == MineshaftType.UNKNOWN) return

        if (event.widget == TabWidget.FROZEN_CORPSES) {
            println(event.lines)
        }
    }

    @HandleEvent
    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {
        mineshaftType = event.type
        if (config.autoLoadWaypoints && mineshaftType.isGemstone) {
            val name = if (mineshaftType.name.endsWith("2")) "CRYSTAL"
            else mineshaftType.name

            if (EurybiumMod.orderedWaypointsRoutes.routes?.keys?.contains(name) == true ) {
                getMinecraft().player?.networkHandler?.sendCommand("eybo load $name")
            }
        }
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
}