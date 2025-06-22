package github.businessdirt.eurybium.data.model

import github.businessdirt.eurybium.utils.SkyBlockUtils
import java.util.*

enum class IslandType(val displayName: String) {
    PRIVATE_ISLAND("Private Island"),
    PRIVATE_ISLAND_GUEST("Private Island Guest"),
    THE_END("The End"),
    KUUDRA_ARENA("Kuudra"),
    CRIMSON_ISLE("Crimson Isle"),
    DWARVEN_MINES("Dwarven Mines"),
    DUNGEON_HUB("Dungeon Hub"),
    CATACOMBS("Catacombs"),

    HUB("Hub"),
    DARK_AUCTION("Dark Auction"),
    THE_FARMING_ISLANDS("The Farming Islands"),
    CRYSTAL_HOLLOWS("Crystal Hollows"),
    THE_PARK("The Park"),
    DEEP_CAVERNS("Deep Caverns"),
    GOLD_MINES("Gold Mine"),
    GARDEN("Garden"),
    GARDEN_GUEST("Garden Guest"),
    SPIDER_DEN("Spider's Den"),
    WINTER("Jerry's Workshop"),
    THE_RIFT("The Rift"),
    MINESHAFT("Mineshaft"),
    BACKWATER_BAYOU("Backwater Bayou"),
    GALATEA("Galatea"),

    NONE(""),
    ANY(""),
    UNKNOWN("???"),
    ;

    fun isValidIsland(): Boolean = when (this) {
        NONE, ANY, UNKNOWN -> false
        else -> true
    }

    fun isInIsland(): Boolean = SkyBlockUtils.inSkyblock() && SkyBlockUtils.currentIsland() == this

    fun guestVariant(): IslandType = when (this) {
        PRIVATE_ISLAND -> PRIVATE_ISLAND_GUEST
        GARDEN -> GARDEN_GUEST
        else -> this
    }

    fun hasGuestVariant(): Boolean = when (this) {
        PRIVATE_ISLAND, GARDEN -> true
        else -> false
    }

    companion object {
        fun getByName(name: String): IslandType? = entries.find { it.displayName.equals(name, ignoreCase = true) }
    }
}
