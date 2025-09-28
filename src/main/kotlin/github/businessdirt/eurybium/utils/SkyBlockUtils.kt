package github.businessdirt.eurybium.utils

import gg.essential.universal.UMinecraft.getPlayer
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import github.businessdirt.eurybium.data.HypixelData
import github.businessdirt.eurybium.data.model.IslandType

@Suppress("unused")
object SkyBlockUtils {
    fun onHypixel(): Boolean = HypixelData.connectedToHypixel && getPlayer() != null

    fun inSkyblock(): Boolean = HypixelData.connectedToHypixel && HypixelData.skyBlock

    fun currentIsland(): IslandType = HypixelData.skyBlockIsland

    fun scoreboardArea(): String? = if (inSkyblock()) HypixelData.skyBlockArea else null

    fun lastWorldSwitch(): SimpleTimeMark = HypixelData.joinedWorld

    fun debug(): Boolean = onHypixel() && EurybiumMod.config.dev.debug.enabled

    fun inAnyIsland(vararg islandTypes: IslandType): Boolean = inSkyblock() && islandTypes.any { it == currentIsland() }

    fun inAnyIsland(islandTypes: Collection<IslandType>): Boolean = inSkyblock() && islandTypes.contains(currentIsland())
}
