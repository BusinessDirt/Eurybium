package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.api.BazaarAPI
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.events.skyblock.MineshaftEnteredEvent
import github.businessdirt.eurybium.features.types.GemstoneType
import kotlin.math.pow

object ProfitableMineshaft {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft
    private var corpseCount: Int = 0

    @HandleEvent()
    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {
        val gemstone = GemstoneType.fromMineshaftType(event.type) ?: return
        val fineGemstonePrice = BazaarAPI.getProduct("FINE_${gemstone.name}_GEM")?.sellPrice ?: return
        val normalizedFineGemstonePrice = normalizeGemstonePrice(fineGemstonePrice, gemstone.blockStrength, corpseCount, config.mineshaftCorpseMultiplier)

        if (normalizedFineGemstonePrice >= config.gemstonePriceThreshold) {
            EurybiumMod.logger.info(
                "Normalized price ($normalizedFineGemstonePrice) is higher than ${config.gemstonePriceThreshold}. " +
                        "You should mine in this mineshaft!"
            )
        } else EurybiumMod.logger.debug("Normalized price ($normalizedFineGemstonePrice) is lower than ${config.gemstonePriceThreshold}.")
    }

    @HandleEvent()
    fun onTabWidgetUpdateEvent(event: TabWidgetUpdateEvent) {
        if (event.widget == TabWidget.FROZEN_CORPSES) {
            event.widget.lines.forEach { EurybiumMod.logger.debug(it) }
        }
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
