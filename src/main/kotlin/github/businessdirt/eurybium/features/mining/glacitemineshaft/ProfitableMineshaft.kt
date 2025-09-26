package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.events.HandleEvent
import github.businessdirt.eurybium.data.model.TabWidget
import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.events.hypixel.MineshaftEnteredEvent
import github.businessdirt.eurybium.features.types.GemstoneType
import kotlin.math.pow

object ProfitableMineshaft {

    private val config get() = EurybiumMod.config.mining.glaciteMineshaft
    private var corpseCount: Int = 0

    @HandleEvent()
    fun onMineshaftEnteredEvent(event: MineshaftEnteredEvent) {
        val gemstone = GemstoneType.fromMineshaftType(event.type) ?: return
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
