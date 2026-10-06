package github.businessdirt.eurybium.core.rendering

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.rendering.glow.GlowingBlockRenderProfile
import github.businessdirt.eurybium.core.rendering.glow.GlowingBlockRendererTestCommands
import github.businessdirt.eurybium.events.CommandRegistrationEvent
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.lang.reflect.Proxy
import kotlin.test.*

class GlowingBlockRendererTestCommandsTest {
    private var previousDevCommands = false

    @BeforeTest
    fun enableDevCommands() {
        previousDevCommands = EurybiumMod.config.dev.devCommands
        EurybiumMod.config.dev.devCommands = true
    }

    @AfterTest
    fun restoreDevCommands() {
        EurybiumMod.config.dev.devCommands = previousDevCommands
    }

    @Test
    fun `test commands parse completely including coordinates colors and controls`() {
        val dispatcher = CommandDispatcher<FabricClientCommandSource>()
        GlowingBlockRendererTestCommands::class.java.getDeclaredMethod(
            "onCommandRegistrationEvent", CommandRegistrationEvent::class.java,
        ).apply { isAccessible = true }.invoke(GlowingBlockRendererTestCommands, CommandRegistrationEvent(dispatcher))
        val source = Proxy.newProxyInstance(
            FabricClientCommandSource::class.java.classLoader, arrayOf(FabricClientCommandSource::class.java),
        ) { _, method, _ -> error("Unexpected source call: ${method.name}") } as FabricClientCommandSource
        for (arguments in listOf(
            "", "render", "render chroma", "render FF00AA", "render 10 64 -20",
            "render 10 64 -20 80FF0000", "fill 0", "fill 8 blue", "colors 4",
            "remove", "remove 10 64 -20", "clear", "pause", "resume", "status", "profile", "profile reset",
        )) {
            val command = "eybglowtest $arguments".trim()
            val parsed = dispatcher.parse(command, source)
            assertFalse(parsed.reader.canRead(), command)
            assertNotNull(parsed.context.command, command)
        }
        for (invalid in listOf("fill -1", "fill 9", "colors 9")) {
            val parsed = dispatcher.parse("eybglowtest $invalid", source)
            assertFailsWith<CommandSyntaxException>(invalid) { dispatcher.execute(parsed) }
        }
    }

    @Test
    fun `colors preserve channels alpha and chroma timing`() {
        assertEquals(0xFFFF0000.toInt(), GlowingBlockRendererTestCommands.color("red").getEffectiveColourRGB())
        assertEquals(0xFF00AA11.toInt(), GlowingBlockRendererTestCommands.color("#00AA11").getEffectiveColourRGB())
        assertEquals(0x80123456.toInt(), GlowingBlockRendererTestCommands.color("80123456").getEffectiveColourRGB())
        assertEquals(4000, GlowingBlockRendererTestCommands.color("chroma").timeForFullRotationInMillis)
        assertFailsWith<CommandSyntaxException> { GlowingBlockRendererTestCommands.color("not-a-color") }
    }

    @Test
    fun `CPU profile records totals peaks and resets`() {
        val oldDev = EurybiumMod.config.dev.devCommands
        val oldDebug = EurybiumMod.config.dev.debug.enabled
        EurybiumMod.config.dev.devCommands = true
        EurybiumMod.config.dev.debug.enabled = true

        val profile = GlowingBlockRenderProfile()
        profile.recordPreparation(50, { 2 }, { 4 })
        profile.recordSubmission(30)
        profile.recordPreparation(100, { 3 }, { 6 })
        profile.recordSubmission(10)
        assertEquals(2L, profile.samples)
        assertEquals(150L, profile.preparationTotalNs)
        assertEquals(40L, profile.submissionTotalNs)
        assertEquals(100L, profile.preparationMaxNs)
        assertEquals(30L, profile.submissionMaxNs)
        assertEquals(3, profile.submittedBlocks)
        assertEquals(6, profile.modelParts)
        profile.reset()
        assertEquals(0L, profile.samples)
        assertEquals(0L, profile.preparationTotalNs)
        assertEquals(0L, profile.submissionTotalNs)
        assertEquals(0, profile.submittedBlocks)

        EurybiumMod.config.dev.devCommands = oldDev
        EurybiumMod.config.dev.debug.enabled = oldDebug
    }
}
