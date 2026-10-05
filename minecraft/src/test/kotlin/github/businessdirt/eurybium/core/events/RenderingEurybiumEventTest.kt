package github.businessdirt.eurybium.core.events

import com.mojang.blaze3d.vertex.PoseStack
import github.businessdirt.eurybium.events.minecraft.rendering.WorldRenderLastEvent
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.renderer.state.level.LevelRenderState
import net.minecraft.world.phys.Vec3
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class RenderingEurybiumEventTest {
    @Test
    fun `last render event constructs with its context and an independent camera offset stack`() {
        val poseStack = PoseStack().apply { translate(1f, 2f, 3f) }
        val state = LevelRenderState().apply { cameraRenderState.pos = Vec3(10.0, 20.0, 30.0) }
        val context = Proxy.newProxyInstance(
            LevelRenderContext::class.java.classLoader,
            arrayOf(LevelRenderContext::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "poseStack" -> poseStack
                "levelState" -> state
                else -> error("Unexpected context call: ${method.name}")
            }
        } as LevelRenderContext

        val event = WorldRenderLastEvent(context)
        assertSame(context, event.context)
        val matrix = event.matrixStack.toMC().last().pose()
        assertEquals(-9f, matrix.m30())
        assertEquals(-18f, matrix.m31())
        assertEquals(-27f, matrix.m32())
        event.matrixStack.translate(5f, 6f, 7f)
        assertEquals(1f, poseStack.last().pose().m30())
        assertEquals(2f, poseStack.last().pose().m31())
        assertEquals(3f, poseStack.last().pose().m32())
    }
}
