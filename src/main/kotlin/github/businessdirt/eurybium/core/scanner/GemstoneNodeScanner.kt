package github.businessdirt.eurybium.core.scanner

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

private val GEMSTONE_MINECRAFT_BLOCKS: Set<Block> = setOf(
    Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE, // Ruby
    Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE, // Amber
    Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, // Sapphire
    Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE, // Jade
    Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE, // Amethyst
    Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE, // Opal
    Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE, // Topaz
    Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE, // Jasper
    Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE, // Onyx
    Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE, // Aquamarine
    Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE, // Citrine
    Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE, // Peridot
)

class GemstoneNodeScanner : BlockScanner(GEMSTONE_MINECRAFT_BLOCKS) {

    /**
     * Groups blocks into connected clusters (connected components).
     *
     * @param includeDiagonals if true, uses 26-neighbour connectivity; otherwise uses 6-face connectivity
     * @return list of clusters; each cluster is a Map<Identifier, Set<BlockPos>> containing only positions in that cluster
     */
    fun clusterBlocks(includeDiagonals: Boolean = false): List<Map<Identifier, Set<BlockPos>>> {

        // Build quick lookup from BlockPos -> Identifier. Also detect duplicates.
        val posToId = HashMap<BlockPos, Identifier>(foundBlocks.values.sumOf { it.size })
        for ((id, poses) in foundBlocks) {
            for (pos in poses) {
                val prev = posToId.putIfAbsent(pos, id)
                if (prev != null) {
                    throw IllegalArgumentException("BlockPos $pos appears for both $prev and $id")
                }
            }
        }

        if (posToId.isEmpty()) return emptyList()

        // Precompute neighbour deltas
        val deltas = if (includeDiagonals) {
            val list = mutableListOf<Triple<Int, Int, Int>>()
            for (dx in -1..1) for (dy in -1..1) for (dz in -1..1) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                list += Triple(dx, dy, dz)
            }
            list
        } else {
            listOf(
                Triple(1, 0, 0), Triple(-1, 0, 0),
                Triple(0, 1, 0), Triple(0, -1, 0),
                Triple(0, 0, 1), Triple(0, 0, -1)
            )
        }

        val visited = HashSet<BlockPos>(posToId.size)
        val clusters = mutableListOf<Map<Identifier, Set<BlockPos>>>()

        for (start in posToId.keys) {
            if (!visited.add(start)) continue // already part of a found cluster

            // BFS queue
            val queue = ArrayDeque<BlockPos>()
            queue.add(start)

            // cluster accumulator: Identifier -> positions in this cluster
            val clusterMap = mutableMapOf<Identifier, MutableSet<BlockPos>>()

            while (queue.isNotEmpty()) {
                val p = queue.removeFirst()
                // add to cluster map
                val id = posToId[p]!! // guaranteed to exist
                clusterMap.computeIfAbsent(id) { mutableSetOf() }.add(p)

                // check neighbours
                for ((dx, dy, dz) in deltas) {
                    val np = BlockPos(p.x + dx, p.y + dy, p.z + dz)
                    if (posToId.containsKey(np) && visited.add(np)) {
                        queue.add(np)
                    }
                }
            }

            // freeze the sets/maps if you prefer immutability at the API boundary
            clusters.add(clusterMap.mapValues { it.value.toSet() })
        }

        return clusters
    }
}