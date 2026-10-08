package github.businessdirt.eurybium.events.skyblock

import github.businessdirt.eurybium.api.events.EurybiumEvent

/**
 * Fired when the area name shown on the SkyBlock scoreboard changes.
 *
 * @param area the area name currently shown on the scoreboard.
 * @param previousArea the area name shown before this change, or null when no area was known yet.
 */
class SkyblockAreaChangeEvent(val area: String, val previousArea: String?) : EurybiumEvent()
