package github.businessdirt.eurybium.core.rendering.glow

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.lang.reflect.Proxy
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.data.repo.*
import kotlin.test.*

class NodeRendererTestCommandsTest {
    private fun node(id: String, file: String) = RepoMiningNode(
        id, MineshaftType.JASP_1, "JASPER", listOf(RepoPosition(1, 2, 3)),
        listOf("minecraft:magenta_stained_glass"), file,
    )

    private fun snapshot(vararg nodes: RepoMiningNode) = RepoSnapshot("", 0, emptyMap(), emptyMap(), nodes.associateBy { it.id })

    @Test
    fun `command forms parse and disabled dev commands cannot execute`() {
        val old = EurybiumMod.config.dev.devCommands
        try {
            EurybiumMod.config.dev.devCommands = true
            val dispatcher = CommandDispatcher<FabricClientCommandSource>()
            NodeRendererTestCommands.onCommandRegistration(CommandRegistrationEvent(dispatcher))
            val source = Proxy.newProxyInstance(
                FabricClientCommandSource::class.java.classLoader, arrayOf(FabricClientCommandSource::class.java),
            ) { _, method, _ -> error("Unexpected source call: ${method.name}") } as FabricClientCommandSource
            for (suffix in listOf("", "add JASP1 1", "add JASP1 10 magenta", "addall", "addall JASP1", "list", "list JASP1")) {
                val input = "eybnodetest $suffix".trim()
                val parsed = dispatcher.parse(input, source)
                assertFalse(parsed.reader.canRead(), input)
                assertNotNull(parsed.context.command, input)
            }
            assertFailsWith<CommandSyntaxException> { dispatcher.execute("eybnodetest add JASP1 0", source) }
            val stale = dispatcher.parse("eybnodetest addall", source)
            EurybiumMod.config.dev.devCommands = false
            assertFailsWith<CommandSyntaxException> { dispatcher.execute(stale) }
            assertTrue(dispatcher.getCompletionSuggestions(dispatcher.parse("eybn", source)).join().list.isEmpty())
        } finally {
            EurybiumMod.config.dev.devCommands = old
        }
    }

    @Test
    fun `selection uses file provenance and preserves JSON order without assuming node IDs`() {
        val first = node("arbitrary-id", "mining/nodes/JASP1.json")
        val second = node("another-id", "mining/nodes/JASP1.json")
        val unrelated = node("JASP1/misleading", "mining/nodes/JASPC.json")
        val repo = snapshot(first, unrelated, second)
        assertEquals(listOf("JASP1", "JASPC"), NodeRendererTestCommands.files(repo))
        assertEquals(listOf(first, second), NodeRendererTestCommands.nodesForFile(repo, "jasp1"))
        assertEquals(listOf(first, second), NodeRendererTestCommands.nodesForFile(repo, "JASP1.json"))
        assertEquals(listOf(unrelated), NodeRendererTestCommands.nodesForFile(repo, "JASPC"))
    }

    @Test
    fun `missing files produce command errors`() {
        assertFailsWith<CommandSyntaxException> { NodeRendererTestCommands.nodesForFile(RepoSnapshot.EMPTY, "JASP1") }
    }
}
