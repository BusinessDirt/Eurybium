package github.businessdirt.eurybium.core.rendering.glow

import github.businessdirt.eurybium.data.model.MiningNodeMaterial
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil

/** A live cluster's geometry. Rendering checks its material again as blocks are mined. */
internal class ScannedMiningNode(val positions: List<BlockPos>, val material: String) {
    val blocks = positions.map(::GlowingBlock)
    val center = Vec3(
        positions.sumOf { it.x.toDouble() + 0.5 } / positions.size,
        positions.sumOf { it.y.toDouble() + 0.5 } / positions.size,
        positions.sumOf { it.z.toDouble() + 0.5 } / positions.size,
    )
}

/**
 * Maps block IDs to matching materials. Glass and panes of one color form a single gemstone node;
 * normal and deepslate ores of the same resource also match. Unrelated building blocks are ignored.
 */
internal fun miningMaterial(
    blockId: String,
    dwarvenMaterials: Boolean = false,
    inCrystalHollows: Boolean = false,
): String? = MiningNodeMaterial.forBlock(blockId, dwarvenMaterials, inCrystalHollows)?.id

/**
 * Finds the nearest eligible block inside a spherical range, then traverses its six face neighbors.
 * [readBlock] returns null for unloaded/out-of-world positions and an ID for loaded blocks (including
 * air). [checkpoint] runs before each read so the caller can enforce a shared per-tick work budget.
 * An incomplete or oversized cluster returns null rather than publishing a misleading partial node.
 */
internal suspend fun scanMiningNode(
    origin: BlockPos,
    range: Double,
    preferredMaterial: String?,
    readBlock: (BlockPos) -> String?,
    checkpoint: suspend () -> Unit,
    allowedMaterials: Set<MiningNodeMaterial> = MiningNodeMaterial.entries.toSet(),
    dwarvenMaterials: Boolean = false,
    inCrystalHollows: Boolean = false,
): ScannedMiningNode? {
    if (!range.isFinite() || range !in 0.0..32.0 || allowedMaterials.isEmpty()) return null
    val preference = preferredMaterial?.let { MiningNodeMaterial.fromId(it) ?: return null }
    if (preference != null && preference !in allowedMaterials) return null
    fun eligible(id: String): MiningNodeMaterial? = MiningNodeMaterial.forBlock(id, dwarvenMaterials, inCrystalHollows)
        ?.takeIf { it in allowedMaterials && (preference == null || it == preference) }
    val radius = ceil(range).toInt()
    checkpoint()
    val originId = readBlock(origin) ?: return null
    val originMaterial = eligible(originId)
    var seed: BlockPos? = origin.takeIf { originMaterial != null }
    var material = originMaterial
    var nearestDistance = if (seed != null) 0.0 else Double.POSITIVE_INFINITY
    var incomplete = false

    // A waypoint already on its desired material has the closest possible seed. This avoids a
    // large radius search entirely for the usual case of routes placed directly on mining blocks.
    if (seed == null) {
        // Fixed order makes equal-distance choices deterministic without sorting a large cube.
        for (dx in -radius..radius) for (dy in -radius..radius) for (dz in -radius..radius) {
            val distance = (dx * dx + dy * dy + dz * dz).toDouble()
            if (distance == 0.0 || distance > range * range) continue
            val position = origin.offset(dx, dy, dz)
            checkpoint()
            val id = readBlock(position)
            if (id == null) {
                incomplete = true
                continue
            }
            val candidate = eligible(id) ?: continue
            if (distance < nearestDistance) {
                seed = position
                material = candidate
                nearestDistance = distance
            }
        }
    }
    // An unloaded part of the search could contain a nearer candidate. Retry after chunks arrive.
    if (incomplete) return null
    val start = seed ?: return null
    val selectedMaterial = material ?: return null
    val queue = ArrayDeque<BlockPos>()
    val visited = hashSetOf(start)
    val positions = mutableListOf<BlockPos>()
    queue.add(start)

    while (queue.isNotEmpty()) {
        val position = queue.removeFirst()
        checkpoint()
        val id = readBlock(position) ?: return null
        if (eligible(id) != selectedMaterial) continue
        positions.add(position)
        if (positions.size > MAX_MINING_NODE_BLOCKS) return null
        for (direction in net.minecraft.core.Direction.entries) {
            val neighbor = position.relative(direction)
            if (visited.add(neighbor)) queue.add(neighbor)
        }
    }
    return positions.takeIf { it.isNotEmpty() }?.let { ScannedMiningNode(it.toList(), selectedMaterial.id) }
}

internal const val MAX_MINING_NODE_BLOCKS = 2048
