package github.businessdirt.eurybium.core.scanner

import github.businessdirt.eurybium.config.GemstoneNode
import github.businessdirt.eurybium.core.rendering.GlowingBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
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
     * @return list of clusters; each cluster is a Map<Identifier, Set<BlockPos>> containing only positions in that cluster
     */
    fun clusterBlocks(): MutableList<GemstoneNode> {
        val deltas = listOf(
            BlockPos(1, 0, 0), BlockPos(-1, 0, 0),
            BlockPos(0, 1, 0), BlockPos(0, -1, 0),
            BlockPos(0, 0, 1), BlockPos(0, 0, -1)
        )

        val visited = HashSet<BlockPos>(foundBlocks.size)
        val clusters = mutableListOf<GemstoneNode>()

        for (start in foundBlocks) {
            if (!visited.add(start)) continue // already part of a found cluster

            // BFS queue
            val queue = ArrayDeque<BlockPos>().apply { add(start) }
            val gemstoneNode = GemstoneNode()

            while (queue.isNotEmpty()) {
                val position = queue.removeFirst()
                gemstoneNode.blocks.add(GlowingBlock(position))

                // check neighbours
                for (delta in deltas) {
                    val neighbour = position.add(delta)
                    if (neighbour in foundBlocks && visited.add(neighbour)) {
                        queue.add(neighbour)
                    }
                }
            }

            if (gemstoneNode.blocks.isNotEmpty()) {
                val centerVec = gemstoneNode.centerVec()

                // find the closest block to the average position
                gemstoneNode.centerNodeIndex = gemstoneNode.blocks.withIndex()
                    .minByOrNull { (_, block) -> block.position.getSquaredDistance(centerVec) }
                    ?.index ?: -1
            }

            clusters.add(gemstoneNode)
        }

        return clusters
    }
}