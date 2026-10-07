package github.businessdirt.eurybium.core.rendering.glow

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierArguments
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.minecraft.WorldChangeEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import io.github.notenoughupdates.moulconfig.ChromaColour
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import java.util.*

/** Persistent test selections are resubmitted every frame, independently of feature requests. */
@EurybiumModule
object GlowingBlockRendererTestCommands {

    private const val MAX_BLOCKS = 4096

    private val selections = linkedMapOf<BlockPos, ChromaColour>()
    private var enabled = true
    private val colorNames = listOf("cyan", "red", "green", "blue", "yellow", "magenta", "white", "chroma")

    @HandleEvent
    private fun onCommandRegistrationEvent(event: CommandRegistrationEvent) = event.register("eybglowtest") {
        category = CommandCategory.DEVELOPER_TEST
        description = "Test block model outlines and profile their CPU preparation/submission."

        callback { feedback(context.source as FabricClientCommandSource, HELP) }

        literal("render") {
            callback { select(context.source as FabricClientCommandSource, target(), "cyan") }

            argCallback("color", BrigadierArguments.word(), colorNames) { color ->
                select(context.source as FabricClientCommandSource, target(), color)
            }

            arg("x", BrigadierArguments.int()) { x ->
                arg("y", BrigadierArguments.int()) { y ->
                    arg("z", BrigadierArguments.int()) { z ->
                        callback { select(context.source as FabricClientCommandSource, BlockPos(getArg(x), getArg(y), getArg(z)), "cyan") }

                        argCallback("color", BrigadierArguments.word(), colorNames) { color ->
                            select(context.source as FabricClientCommandSource, BlockPos(getArg(x), getArg(y), getArg(z)), color)
                        }
                    }
                }
            }
        }

        literal("fill") {
            arg("radius", BrigadierArguments.int(0, 8)) { radius ->
                callback { fill(context.source as FabricClientCommandSource, getArg(radius), "cyan") }

                argCallback("color", BrigadierArguments.word(), colorNames) { color ->
                    fill(context.source as FabricClientCommandSource, getArg(radius), color)
                }
            }
        }

        literal("colors") {
            argCallback("radius", BrigadierArguments.int(0, 8)) { radius ->
                fill(context.source as FabricClientCommandSource, radius, null)
            }
        }

        literal("remove") {
            callback { remove(context.source as FabricClientCommandSource, target()) }

            arg("x", BrigadierArguments.int()) { x ->
                arg("y", BrigadierArguments.int()) { y ->
                    argCallback("z", BrigadierArguments.int()) { z ->
                        remove(context.source as FabricClientCommandSource, BlockPos(getArg(x), getArg(y), z))
                    }
                }
            }
        }

        literal("clear") {
            callback {
                selections.clear()
                feedback(context.source as FabricClientCommandSource, "Cleared test selections.")
            }
        }

        literal("pause") {
            callback { enabled = false; feedback(context.source as FabricClientCommandSource, "Test outlines paused.") }
        }

        literal("resume") {
            callback { enabled = true; feedback(context.source as FabricClientCommandSource, "Test outlines resumed.") }
        }

        literal("status") {
            callback { feedback(context.source as FabricClientCommandSource, status()) }
        }

        literal("profile") {
            callback { feedback(context.source as FabricClientCommandSource, profileSummary()) }

            literal("reset") {
                callback {
                    GlowingBlockRenderer.profile.reset()
                    feedback(context.source as FabricClientCommandSource, "CPU profile reset. Use profile after sampling several seconds.")
                }
            }
        }
    }

    /** Bulk imports share the same bounded selection and lifecycle as manual glow tests. */
    internal fun addTestBlocks(blocks: Sequence<Pair<BlockPos, ChromaColour>>): String {
        val level = Minecraft.getInstance().level ?: fail("Join a world first.")
        var selected = 0
        var skipped = 0
        var limited = 0
        for ((pos, color) in blocks) {
            if (pos !in selections && selections.size >= MAX_BLOCKS) { limited++; continue }
            if (!level.hasChunk(pos.x shr 4, pos.z shr 4)) { skipped++; continue }
            val state = level.getBlockState(pos)
            if (state.isAir || state.renderShape != RenderShape.MODEL) { skipped++; continue }
            selections[pos.immutable()] = color
            selected++
        }
        return "Selected/updated $selected blocks, skipped $skipped unsupported/unloaded, capped $limited. " +
            "${selections.size}/$MAX_BLOCKS total; ${if (enabled) "active" else "paused"}."
    }

    internal fun queueTestBlocks() {
        if (!enabled || selections.isEmpty()) return
        val level = Minecraft.getInstance().level ?: return

        selections.forEach { (pos, color) ->
            if (level.hasChunk(pos.x shr 4, pos.z shr 4)) GlowingBlockRenderer.blocks.add(color, GlowingBlock(pos))
        }
    }

    @HandleEvent(eventTypes = [WorldChangeEvent::class, ClientDisconnectEvent::class])
    private fun onWorldClearEvents() {
        selections.clear()
        enabled = true
        GlowingBlockRenderer.profile.reset()
    }

    private fun target(): BlockPos {
        val hit = Minecraft.getInstance().hitResult
        if (hit !is BlockHitResult || hit.type != HitResult.Type.BLOCK) fail("Look at a block, or use explicit x y z coordinates.")

        return hit.blockPos
    }

    private fun select(source: FabricClientCommandSource, pos: BlockPos, colorText: String) {
        val color = color(colorText)
        val level = Minecraft.getInstance().level ?: fail("Join a world first.")
        if (!level.hasChunk(pos.x shr 4, pos.z shr 4)) fail("The block's chunk is not loaded.")

        val state = level.getBlockState(pos)
        if (state.isAir || state.renderShape != RenderShape.MODEL) fail("This block has no regular block model to outline.")
        if (pos !in selections && selections.size >= MAX_BLOCKS) fail("Limit of $MAX_BLOCKS test blocks reached. Clear or remove selections first.")

        selections[pos.immutable()] = color

        feedback(source, "Selected ${pos.toShortString()} ($colorText). ${selections.size} test blocks; ${if (enabled) "active" else "paused"}.")
    }

    private fun fill(source: FabricClientCommandSource, radius: Int, colorText: String?) {
        val center = target()
        val level = Minecraft.getInstance().level ?: fail("Join a world first.")
        val palette = if (colorText == null) colorNames.take(7).map(::color) else listOf(color(colorText))
        var selected = 0
        var skipped = 0
        var limited = 0

        for (x in -radius..radius) for (y in -radius..radius) for (z in -radius..radius) {
            val pos = center.offset(x, y, z)
            if (!level.hasChunk(pos.x shr 4, pos.z shr 4)) { skipped++; continue }

            val state = level.getBlockState(pos)
            if (state.isAir || state.renderShape != RenderShape.MODEL) { skipped++; continue }
            if (pos !in selections && selections.size >= MAX_BLOCKS) { limited++; continue }

            selections[pos] = palette[selected % palette.size]
            selected++
        }

        feedback(source, "Selected/updated $selected blocks, skipped $skipped unsupported/unloaded positions, capped $limited. ${selections.size} total; ${if (enabled) "active" else "paused"}.")
    }

    private fun remove(source: FabricClientCommandSource, pos: BlockPos) =
        feedback(source, if (selections.remove(pos) != null) "Removed ${pos.toShortString()}." else "No test selection at ${pos.toShortString()}.")

    internal fun color(text: String): ChromaColour {
        val normalized = text.lowercase(Locale.ROOT)
        if (normalized == "chroma") return ChromaColour.fromRGB(255, 0, 0, 4000, 255)

        val named = when (normalized) {
            "cyan" -> 0x00FFFF; "red" -> 0xFF0000; "green" -> 0x00FF00; "blue" -> 0x0000FF
            "yellow" -> 0xFFFF00; "magenta" -> 0xFF00FF; "white" -> 0xFFFFFF
            else -> null
        }

        val hex = normalized.removePrefix("#")
        val value = named?.toLong() ?: if (hex.matches(Regex("[0-9a-f]{6}|[0-9a-f]{8}"))) hex.toLong(16)
            else fail("Color must be a named color, chroma, RRGGBB, or AARRGGBB.")

        val alpha = if (named == null && hex.length == 8) ((value ushr 24) and 255).toInt() else 255

        return ChromaColour.fromStaticRGB(((value ushr 16) and 255).toInt(), ((value ushr 8) and 255).toInt(), (value and 255).toInt(), alpha)
    }

    private fun status(): String = "${selections.size}/$MAX_BLOCKS test selections; ${if (enabled) "active" else "paused"}. Last frame: ${GlowingBlockRenderer.profile.submittedBlocks} total renderer blocks, ${GlowingBlockRenderer.profile.modelParts} model parts (all features)."

    private fun profileSummary(): String {
        val p = GlowingBlockRenderer.profile
        if (p.samples == 0L) return "No rendered frames sampled yet."

        fun ms(ns: Double) = String.format(Locale.ROOT, "%.3f", ns / 1_000_000.0)

        return "${p.samples} frames: prepare avg/max ${ms(p.preparationTotalNs.toDouble() / p.samples)}/${ms(p.preparationMaxNs.toDouble())} ms; submit avg/max ${ms(p.submissionTotalNs.toDouble() / p.samples)}/${ms(p.submissionMaxNs.toDouble())} ms. CPU only; excludes model drawing and GPU/post-process cost."
    }

    private fun feedback(source: FabricClientCommandSource, text: String) = source.sendFeedback(Component.literal("[Glow test] $text"))
    private fun fail(message: String): Nothing = throw SimpleCommandExceptionType(Component.literal(message)).create()

    private const val HELP = "render [color] | render <x> <y> <z> [color] | fill <radius 0..8> [color] | colors <radius 0..8> | remove [x y z] | clear | pause | resume | status | profile [reset]. Colors: cyan/red/green/blue/yellow/magenta/white/chroma or hex RRGGBB/AARRGGBB."
}
