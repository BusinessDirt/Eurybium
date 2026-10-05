package github.businessdirt.eurybium.core.utils

import gg.essential.universal.UMinecraft.getPlayer
import github.businessdirt.eurybium.data.HypixelData
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.core.types.SimpleTimeMark

@Suppress("unused")
object SkyBlockUtils {
    fun onHypixel(): Boolean = HypixelData.connectedToHypixel && getPlayer() != null

    fun inSkyblock(): Boolean = HypixelData.connectedToHypixel && HypixelData.skyBlock

    fun currentIsland(): IslandType = HypixelData.skyBlockIsland

    fun scoreboardArea(): String? = if (inSkyblock()) HypixelData.skyBlockArea else null

    fun lastWorldSwitch(): SimpleTimeMark = HypixelData.joinedWorld


    fun inAnyIsland(vararg islandTypes: IslandType): Boolean = inSkyblock() && islandTypes.any { it == currentIsland() }

    fun inAnyIsland(islandTypes: Collection<IslandType>): Boolean = inSkyblock() && islandTypes.contains(currentIsland())
}
