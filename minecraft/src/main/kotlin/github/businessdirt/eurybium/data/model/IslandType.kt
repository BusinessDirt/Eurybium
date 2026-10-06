package github.businessdirt.eurybium.data.model

import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI

enum class IslandType(private val displayName: String, private val apiName: String?) {
    // General
    PRIVATE_ISLAND("Private Island", "dynamic"),
    PRIVATE_ISLAND_GUEST("Private Island Guest", null),
    HUB("Hub", "hub"),
    DARK_AUCTION("Dark Auction", "dark_auction"),
    WINTER("Jerry's Workshop", "winter"),

    // Farming
    THE_FARMING_ISLANDS("The Farming Islands", "farming_1"),
    GARDEN("Garden", "garden"),
    GARDEN_GUEST("Garden Guest", null),

    // Mining
    GOLD_MINES("Gold Mine", "mining_1"),
    DEEP_CAVERNS("Deep Caverns", "mining_2"),
    DWARVEN_MINES("Dwarven Mines", "mining_3"),
    CRYSTAL_HOLLOWS("Crystal Hollows", "crystal_hollows"),
    MINESHAFT("Mineshaft", "mineshaft"),

    // Fishing
    BACKWATER_BAYOU("Backwater Bayou", "fishing_1"),
    LOTUS_ATOLL("Lotus Atoll", "lotus_atoll"),

    // Foraging
    THE_PARK("The Park", "foraging_1"),
    GALATEA("Moonglade Marsh", "foraging_2"),
    TORRHUS_CANYON("Torrhus Canyon", "foraging_3"),

    // Combat
    SPIDER_DEN("Spider's Den", "combat_1"),
    THE_END("The End", "combat_3"),
    CRIMSON_ISLE("Crimson Isle", "crimson_isle"),

    // Dungeons
    DUNGEON_HUB("Dungeon Hub", "dungeon_hub"),
    CATACOMBS("Catacombs", "dungeon"),
    KUUDRA_ARENA("Kuudra", "kuudra"),

    // Special
    THE_RIFT("The Rift", "rift"),
    SAFARI("Critter Safari", "safari"),

    // Special values
    NONE("", null),
    ANY("", null),
    UNKNOWN("???", null),
    ;

    fun isValidIsland(): Boolean = when (this) {
        NONE,
        ANY,
        UNKNOWN,
            -> false

        else -> true
    }

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
        fun getByIdOrNull(id: String): IslandType? = entries.find { it.apiName == id }
        fun getByIdOrUnknown(id: String): IslandType = getByIdOrNull(id) ?: UNKNOWN
    }

    fun isInIsland() = HypixelLocationAPI.state.let { it.inSkyBlock && it.island == this }
}
