package github.businessdirt.eurybium.core.scanner

import gg.essential.universal.UMinecraft
import kotlinx.coroutines.*
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max
import kotlin.math.min

open class BlockScanner(val blockSet: Set<Block>) {

    // Stores all found blocks persistently
    val foundBlocks: MutableSet<BlockPos> = mutableSetOf()

    suspend fun find() = coroutineScope {
        val world = UMinecraft.getMinecraft().world ?: return@coroutineScope
        val playerPos = UMinecraft.getMinecraft().player?.blockPos ?: return@coroutineScope

        val renderDistance = 8 * 16
        val minX = playerPos.x - renderDistance
        val maxX = playerPos.x + renderDistance
        val minY = max(playerPos.y - renderDistance, world.bottomY)
        val maxY = min(playerPos.y + renderDistance, world.topYInclusive)
        val minZ = playerPos.z - renderDistance
        val maxZ = playerPos.z + renderDistance

        val chunkStartX = minX shr 4
        val chunkEndX = maxX shr 4
        val chunkStartZ = minZ shr 4
        val chunkEndZ = maxZ shr 4

        val results = ConcurrentLinkedQueue<BlockPos>()
        val dispatcher = Dispatchers.Default.limitedParallelism(4)

        // Iterate over all chunks in range
        (chunkStartX .. chunkEndX).flatMap { cx ->
            (chunkStartZ .. chunkEndZ).map { cz ->
                async(dispatcher) {
                    val localFound = mutableListOf<BlockPos>()

                    val startX = cx shl 4
                    val endX = startX + 15
                    val startZ = cz shl 4
                    val endZ = startZ + 15

                    for (x in max(startX, minX)..min(endX, maxX)) {
                        for (y in minY..maxY) {
                            for (z in max(startZ, minZ)..min(endZ, maxZ)) {
                                val blockPos = BlockPos(x, y, z)
                                val block = world.getBlockState(blockPos).block

                                if (block in blockSet) localFound.add(blockPos)
                            }

                        }
                        yield()
                    }

                    results.addAll(localFound)
                }
            }
        }.awaitAll()

        foundBlocks.addAll(results)
    }
}