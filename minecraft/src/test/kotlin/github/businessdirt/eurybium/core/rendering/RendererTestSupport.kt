package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UMatrixStack
import gg.essential.universal.vertex.UVertexConsumer
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.lang.reflect.Proxy
import kotlin.test.assertSame

/** Records emitted vertices without creating a graphics context or GPU buffer. */
internal class VertexRecorder {
    data class Vertex(val position: Vec3, val color: Color)

    val vertices = mutableListOf<Vertex>()
    private var position = Vec3.ZERO
    private var color = Color.WHITE

    val consumer = Proxy.newProxyInstance(
        UVertexConsumer::class.java.classLoader,
        arrayOf(UVertexConsumer::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "pos" -> {
                assertSame(UMatrixStack.UNIT, args!![0], "Positions must already be transformed")
                position = Vec3(args[1] as Double, args[2] as Double, args[3] as Double)
                proxy
            }
            "color" -> {
                color = if (args!!.size == 1) args[0] as Color else {
                    Color(args[0] as Int, args[1] as Int, args[2] as Int, args[3] as Int)
                }
                proxy
            }
            "endVertex" -> {
                vertices += Vertex(position, color)
                null
            }
            else -> error("Unexpected vertex operation: ${method.name}")
        }
    } as UVertexConsumer
}
