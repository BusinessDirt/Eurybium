package github.businessdirt.eurybium.data.model

/** Selectable mining resources, grouped by drops rather than by individual Minecraft block variants. */
enum class MiningNodeMaterial(val label: String, val blockNames: List<String>, val dwarvenOnly: Boolean = false) {
    RUBY("Ruby", listOf("red_stained_glass")),
    AMBER("Amber", listOf("orange_stained_glass")),
    SAPPHIRE("Sapphire", listOf("light_blue_stained_glass")),
    JADE("Jade", listOf("lime_stained_glass")),
    AMETHYST("Amethyst", listOf("purple_stained_glass")),
    TOPAZ("Topaz", listOf("yellow_stained_glass")),
    JASPER("Jasper", listOf("magenta_stained_glass")),
    OPAL("Opal", listOf("white_stained_glass")),
    ONYX("Onyx", listOf("black_stained_glass")),
    AQUAMARINE("Aquamarine", listOf("blue_stained_glass")),
    CITRINE("Citrine", listOf("brown_stained_glass")),
    PERIDOT("Peridot", listOf("green_stained_glass")),
    COAL("Coal", listOf("coal_ore")),
    IRON("Iron", listOf("iron_ore")),
    GOLD("Gold", listOf("gold_ore", "nether_gold_ore", "gold_block")),
    COPPER("Copper", listOf("copper_ore")),
    LAPIS("Lapis", listOf("lapis_ore")),
    REDSTONE("Redstone", listOf("redstone_ore")),
    DIAMOND("Diamond", listOf("diamond_ore")),
    EMERALD("Emerald", listOf("emerald_ore")),
    QUARTZ("Quartz", listOf("nether_quartz_ore")),
    MITHRIL("Mithril", listOf("prismarine", "prismarine_bricks", "dark_prismarine", "gray_wool", "cyan_terracotta", "light_blue_wool", "blue_wool")),
    TITANIUM("Titanium", listOf("polished_diorite"), true),
    TUNGSTEN("Tungsten", listOf("cobblestone", "cobblestone_slab", "cobblestone_stairs", "clay"), true),
    UMBER("Umber", listOf("terracotta", "brown_terracotta", "red_sandstone", "red_sandstone_slab", "red_sandstone_stairs"), true),
    GLACITE("Glacite", listOf("packed_ice"), true),
    ;

    /** Stable material ID used by waypoints, including the historical gemstone/ore block IDs. */
    val id: String = if (name in listOf("MITHRIL", "TITANIUM", "TUNGSTEN", "UMBER", "GLACITE")) {
        "eurybium:${name.lowercase()}"
    } else "minecraft:${blockNames.first()}"

    override fun toString(): String = label

    companion object {
        private val byBlock = entries.flatMap { material -> material.blockNames.map { it to material } }.toMap()

        /** Resolves a loaded block; Dwarven building-block substitutions need a matching region. */
        fun forBlock(blockId: String, region: MiningNodeRegion? = null): MiningNodeMaterial? {
            if (!blockId.startsWith("minecraft:")) return null
            val name = blockId.removePrefix("minecraft:").removePrefix("deepslate_").removeSuffix("_pane")
            val material = byBlock[name] ?: return null
            if (material.dwarvenOnly && region != MiningNodeRegion.MINESHAFT && region != MiningNodeRegion.GLACITE_TUNNELS) return null
            if (region == MiningNodeRegion.CRYSTAL_HOLLOWS && (name == "gray_wool" || name == "cyan_terracotta" || name == "blue_wool")) return null
            return material
        }

        /** Accepts a resource name, canonical material ID, or one of its Minecraft block IDs. */
        fun fromId(value: String): MiningNodeMaterial? {
            val normalized = value.trim().lowercase()
            return entries.find { it.name.lowercase() == normalized || it.id == normalized }
                ?: forBlock(if (':' in normalized) normalized else "minecraft:$normalized", MiningNodeRegion.MINESHAFT)
        }
    }
}
