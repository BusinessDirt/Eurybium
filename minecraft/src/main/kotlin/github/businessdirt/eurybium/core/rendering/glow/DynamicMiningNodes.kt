package github.businessdirt.eurybium.core.rendering.glow

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.hypixelapi.HypixelLocationAPI
import github.businessdirt.eurybium.api.skyblock.MiningAPI
import github.businessdirt.eurybium.config.features.mining.NodeExpansionPolicy
import github.businessdirt.eurybium.data.ScoreboardData
import github.businessdirt.eurybium.data.model.MiningNodeRegion
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.TickEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import kotlin.coroutines.CoroutineContext

/**
 * Bounded live-world scans, cooperatively scheduled on the client thread. Coroutines preserve the
 * traversal between ticks; they never read Minecraft's mutable world on a background dispatcher.
 * Results expire to pick up mined/placed blocks, and world changes cancel all outstanding scans.
 */
@EurybiumModule
object DynamicMiningNodes {
    private data class Key(val position: BlockPos, val range: Double, val material: String?)
    private class Entry(var node: ScannedMiningNode? = null, var expiresAt: Long = Long.MAX_VALUE)

    private val tasks = ArrayDeque<Runnable>()
    private val dispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) { tasks.addLast(block) }
    }
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val cache = linkedMapOf<Key, Entry>()
    private var level: ClientLevel? = null
    private var activePolicy: NodeExpansionPolicy? = null
    private var ticks = 0L
    private var readsRemaining = 0
    private var deadlineNanos = 0L

    /** Returns a cached result immediately; uncached requests use single-block glow while scanning. */
    internal fun request(position: BlockPos, range: Double, material: String?): ScannedMiningNode? {
        val world = getMinecraft().level ?: return null
        val policy = currentPolicy() ?: return null
        if (level !== world || activePolicy != policy) {
            reset()
            level = world
            activePolicy = policy
        }
        if (!range.isFinite() || range !in 0.0..32.0) return null
        val key = Key(position.immutable(), range, material?.trim()?.lowercase()?.takeIf { it.isNotEmpty() })
        val existing = cache[key]
        if (existing != null && ticks < existing.expiresAt) return existing.node
        if (existing != null) cache.remove(key)
        // Keep pending requests too: route changes must not enqueue unbounded scans.
        if (cache.size >= 64) {
            val finished = cache.entries.firstOrNull { it.value.expiresAt != Long.MAX_VALUE } ?: return null
            cache.remove(finished.key)
        }
        // Keep the previous geometry while refreshing to avoid periodic single-block flicker.
        // Rendering still rejects blocks that have since been mined or changed material.
        val entry = existing ?: Entry()
        entry.expiresAt = Long.MAX_VALUE
        cache[key] = entry
        scope.launch {
            try {
                entry.node = scanMiningNode(key.position, key.range, key.material, { pos ->
                    when {
                        world.isOutsideBuildHeight(pos) -> "minecraft:air"
                        !world.hasChunk(pos.x shr 4, pos.z shr 4) -> null
                        else -> BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).block).toString()
                    }
                }, checkpoint = {
                    while (readsRemaining == 0 || System.nanoTime() >= deadlineNanos) yield()
                    readsRemaining--
                }, allowedMaterials = policy.allowedMaterials, region = policy.region)
            } catch (cancelled: kotlin.coroutines.cancellation.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                EurybiumMod.logger.error("Could not scan a waypoint mining node", failure)
            } finally {
                entry.expiresAt = ticks + if (entry.node == null) 20 else 200
            }
        }
        return entry.node
    }

    @HandleEvent(eventType = TickEvent::class)
    private fun onTick() {
        ticks++
        val policy = currentPolicy()
        if (policy != activePolicy) reset()
        if (policy == null) return
        activePolicy = policy
        readsRemaining = 1024
        deadlineNanos = System.nanoTime() + 2_000_000L
        // Process only the queue present at tick start. yield() appends a continuation for next tick.
        // The 1024-read / 2 ms allowance is shared, rather than multiplied by waypoint count.
        // One read may exceed the time budget; the following checkpoint then yields.
        repeat(tasks.size) { tasks.removeFirst().run() }
    }

    @HandleEvent(eventType = WorldChangeEvent::class)
    private fun onWorldChange() = reset()

    @HandleEvent(eventType = ClientDisconnectEvent::class)
    private fun onDisconnect() = reset()

    /** Region substitutions must also be used when checking cached blocks during rendering. */
    internal fun materialAt(blockId: String): String? = miningMaterial(blockId, activePolicy?.region)

    private fun currentPolicy(): NodeExpansionPolicy? {
        // Preserve the existing developer force-toggle for testing mineshafts in downloaded worlds.
        val region = if (MiningAPI.currentMineshaft != null) MiningNodeRegion.MINESHAFT else {
            val location = HypixelLocationAPI.state
            if (!location.inSkyBlock) return null
            MiningNodeRegion.resolve(location.island, ScoreboardData.sidebarLinesFormatted)
        }
        return EurybiumMod.config.mining.waypointNodes.policy(region)
    }

    private fun reset() {
        scope.coroutineContext.cancelChildren()
        // Drain cancelled continuations so their jobs release captured world references.
        while (tasks.isNotEmpty()) tasks.removeFirst().run()
        cache.clear()
        level = null
        activePolicy = null
        readsRemaining = 0
    }
}
