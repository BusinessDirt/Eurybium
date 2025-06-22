package github.businessdirt.eurybium.core.scanner

import gg.essential.universal.UMinecraft
import github.businessdirt.eurybium.core.rendering.BlockOutlineRenderer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.math.max
import kotlin.math.min

open class BlockScanner(val blockSet: Set<Block>) {

    // Stores all found blocks persistently
    val foundBlocks: MutableMap<Identifier, MutableSet<BlockPos>> = mutableMapOf()

    private val mutex = Mutex()

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

        val jobs = mutableListOf<Deferred<Unit>>()

        // Iterate over all chunks in range
        for (chunkX in chunkStartX..chunkEndX) {
            for (chunkZ in chunkStartZ..chunkEndZ) {
                jobs += async(Dispatchers.Default) {
                    val localFound: MutableMap<Identifier, MutableSet<BlockPos>> = mutableMapOf()

                    val startX = chunkX shl 4
                    val endX = startX + 15
                    val startZ = chunkZ shl 4
                    val endZ = startZ + 15

                    for (x in max(startX, minX)..min(endX, maxX)) {
                        for (y in minY..maxY) {
                            for (z in max(startZ, minZ)..min(endZ, maxZ)) {
                                val blockPos = BlockPos(x, y, z)
                                val blockState = world.getBlockState(blockPos)

                                if (blockState.block in blockSet) {
                                    val id = Registries.BLOCK.getId(blockState.block)
                                    localFound.getOrPut(id) { mutableSetOf() }.add(blockPos)
                                }
                            }
                        }
                        yield() // cooperative multitasking
                    }

                    // Append this chunk's results to the global list
                    mutex.withLock {
                        localFound.forEach { (id, blockPositions) ->
                            foundBlocks.getOrPut(id) { mutableSetOf() }.addAll(blockPositions)
                        }
                    }
                }
            }
        }

        // Wait for all chunk jobs
        jobs.awaitAll()
    }
}