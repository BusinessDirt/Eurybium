package github.businessdirt.eurybium.data.model

/** Regions with independent waypoint-node expansion rules. Other locations use single-block glow. */
enum class MiningNodeRegion {
    MINESHAFT, GLACITE_TUNNELS, CRYSTAL_HOLLOWS;

    companion object {
        /**
         * Glacite Tunnels share the Dwarven Mines island ID. Cold and the location line identify that
         * area (including the warm base camp); the island check prevents matching unrelated text.
         */
        fun resolve(island: IslandType, sidebar: List<String>): MiningNodeRegion? = when (island) {
            IslandType.MINESHAFT -> MINESHAFT
            IslandType.CRYSTAL_HOLLOWS -> CRYSTAL_HOLLOWS
            IslandType.DWARVEN_MINES -> GLACITE_TUNNELS.takeIf {
                sidebar.any { line ->
                    '❄' in line || line.trim().startsWith("Cold:") ||
                        ('⏣' in line && ("Glacite" in line || "Dwarven Base Camp" in line))
                }
            }
            else -> null
        }
    }
}
