package github.businessdirt.eurybium.api.minecraft.math

import gg.essential.universal.UMatrixStack

object MatrixExtensions {

    fun UMatrixStack.use(block: UMatrixStack.() -> Unit) {
        push()
        block()
        pop()
    }
}
