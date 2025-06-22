package github.businessdirt.eurybium.core.rendering

import com.mojang.blaze3d.opengl.GlStateManager
import net.minecraft.client.util.math.MatrixStack

object RenderState {
    fun enableCull() = GlStateManager._enableCull()
    fun disableCull() = GlStateManager._disableCull()

    fun MatrixStack.with(action: () -> Unit) {
        this.push()
        action()
        this.pop()
    }
}