package github.businessdirt.eurybium.core.rendering.glow

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import github.businessdirt.eurybium.api.commands.CommandCategory
import github.businessdirt.eurybium.api.commands.brigadier.BrigadierArguments
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.api.repo.RepoAPI
import github.businessdirt.eurybium.data.repo.RepoMiningNode
import github.businessdirt.eurybium.data.repo.RepoSnapshot
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import github.businessdirt.eurybium.processors.EurybiumModule
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import java.util.Locale

/** Loads surveyed nodes into the persistent glow test selection, using their saved world positions. */
@EurybiumModule
object NodeRendererTestCommands {

    private val colors = listOf("cyan", "red", "green", "blue", "yellow", "magenta", "white", "chroma")

    private val fileSuggestions = SuggestionProvider<FabricClientCommandSource> { _, builder ->
        val prefix = builder.remaining.lowercase(Locale.ROOT)
        files(RepoAPI.snapshot).filter { it.lowercase(Locale.ROOT).startsWith(prefix) }.forEach(builder::suggest)
        builder.buildFuture()
    }

    @HandleEvent
    internal fun onCommandRegistration(event: CommandRegistrationEvent) = event.register("eybnodetest") {
        category = CommandCategory.DEVELOPER_TEST
        description = "Add repository node clusters to the glow renderer test selections."

        callback { feedback(HELP) }

        literal("add") {
            arg("file", BrigadierArguments.word(), fileSuggestions) { file ->
                arg("node", BrigadierArguments.int(1)) { index ->
                    callback {
                        val nodes = nodesForFile(RepoAPI.snapshot, getArg(file))
                        val number = getArg(index)
                        val node = nodes.getOrNull(number - 1) ?: fail("Node must be in 1..${nodes.size} for ${getArg(file)}.")
                        add(listOf(node), colors[(number - 1) % 7])
                    }

                    argCallback("color", BrigadierArguments.word(), colors) { color ->
                        val nodes = nodesForFile(RepoAPI.snapshot, getArg(file))
                        val number = getArg(index)
                        val node = nodes.getOrNull(number - 1) ?: fail("Node must be in 1..${nodes.size} for ${getArg(file)}.")
                        add(listOf(node), color)
                    }
                }
            }
        }

        literal("addall") {
            callback {
                val nodes = RepoAPI.snapshot.nodes.values.toList()
                if (nodes.isEmpty()) fail("No mineshaft gemstone nodes loaded. Check /eybrepo.")
                add(nodes)
            }

            argCallback("file", BrigadierArguments.word(), fileSuggestions) { file ->
                add(nodesForFile(RepoAPI.snapshot, file))
            }
        }

        literal("list") {
            callback {
                val names = files(RepoAPI.snapshot)
                feedback(if (names.isEmpty()) "No repo node files loaded. Check /eybrepo." else "Files: ${names.joinToString()}. Node numbers start at 1.")
            }

            argCallback("file", BrigadierArguments.word(), fileSuggestions) { file ->
                val nodes = nodesForFile(RepoAPI.snapshot, file)
                feedback("$file: ${nodes.size} nodes, ${nodes.sumOf { it.blocks.size }} blocks; valid numbers 1..${nodes.size}.")
            }
        }
    }

    /** Use actual source-file metadata rather than relying on a convention in node IDs. */
    internal fun files(snapshot: RepoSnapshot): List<String> = snapshot.nodes.values.map { fileKey(it.sourceFile) }.distinct().sorted()

    /** Node indexes preserve file order and are one-based in the command UI. */
    internal fun nodesForFile(snapshot: RepoSnapshot, file: String): List<RepoMiningNode> {
        val nodes = snapshot.nodes.values.filter { fileKey(it.sourceFile).equals(file.removeSuffix(".json"), ignoreCase = true) }
        if (nodes.isEmpty()) fail("No nodes for file $file. Use /eybnodetest list or /eybrepo refresh.")
        return nodes
    }

    private fun fileKey(path: String): String = path.substringAfterLast('/').removeSuffix(".json")

    private fun add(nodes: List<RepoMiningNode>, color: String? = null) {
        val override = color?.let(GlowingBlockRendererTestCommands::color)

        // Assign one color per cluster; overlapping positions still share the test tool's deduplication.
        val blocks = nodes.asSequence().withIndex().flatMap { (index, node) ->
            val tint = override ?: GlowingBlockRendererTestCommands.color(colors[index % 7])
            node.blocks.asSequence().map { BlockPos(it.x, it.y, it.z) to tint }
        }

        val result = GlowingBlockRendererTestCommands.addTestBlocks(blocks)

        feedback("${nodes.size} node(s): $result Use /eybglowtest clear, pause, resume, status or profile.")
    }

    private fun feedback(text: String) = ChatAPI.debug("[Node test] $text")
    private fun fail(message: String): Nothing = throw SimpleCommandExceptionType(Component.literal(message)).create()

    private const val HELP = "add <file> <node number starting at 1> [color] | addall [file] | list [file]. Uses loaded repository data and the shared glow test selections."
}
