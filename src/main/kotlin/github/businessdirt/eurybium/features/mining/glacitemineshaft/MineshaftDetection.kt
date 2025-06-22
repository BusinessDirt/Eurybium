package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.api.BazaarAPI
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.data.ScoreboardData
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.events.SecondPassedEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.events.skyblock.MineshaftEnteredEvent
import github.businessdirt.eurybium.features.types.GemstoneType
import github.businessdirt.eurybium.features.types.MineshaftType
import kotlin.math.pow

object MineshaftDetection {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft
    private var found = false

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        if (!config.detectMineshaft) return
        found = false
    }

    @HandleEvent(onlyOnIsland = IslandType.MINESHAFT)
    fun onSecondPassedEvent(event: SecondPassedEvent) {
        if (!config.detectMineshaft || found) return

        val matchingLine = ScoreboardData.sidebarLinesFormatted
            .firstOrNull { line -> MineshaftType.entries.any { line.contains(it.name) } }
            ?: return

        val areaName = matchingLine.split(" ").last()

        EurybiumMod.logger.debug("In area: {}", areaName)

        val type = MineshaftType.entries.firstOrNull { areaName.contains(it.name) } ?: return
        found = true

        EurybiumMod.logger.debug("Found a {} mineshaft! [{}]", type.name, areaName)

        MineshaftEnteredEvent(type).post()
    }

    /**
     * Normalizes the price of the gemstone by block strength and the amount of corpses
     * normalized = gemstonePrice * corpseMultiplier^corpseCount
     * @param gemstonePrice price of the gemstone
     * @param corpseCount the corpses in the mineshaft
     * @param corpseMultiplier the scaling or worth of a corpse
     * @return the normalized price
     */
    private fun normalizeGemstonePrice(
        gemstonePrice: Double,
        blockStrength: Int,
        corpseCount: Int,
        corpseMultiplier: Float
    ): Double {
        return gemstonePrice / blockStrength * corpseMultiplier.toDouble().pow(corpseCount.toDouble())
    }
}
