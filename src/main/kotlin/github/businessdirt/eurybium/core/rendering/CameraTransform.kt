package github.businessdirt.eurybium.core.rendering

import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft.getMinecraft
import org.joml.Quaternionf

object CameraTransform {
    /**
     * Applies the camera offset to the given matrices.
     * This is useful for rendering things in world space, as it will negate the camera position.
     */
    fun offset(matrices: UMatrixStack) {
        val pos = getMinecraft().gameRenderer.camera.pos.negate()
        matrices.translate(pos.x, pos.y, pos.z)
    }

    /**
     * Applies the camera rotation to the given matrices.
     * This is useful for rendering things in world space, as it will negate the camera rotation.
     */
    fun rotation(matrices: UMatrixStack) {
        matrices.multiply(getMinecraft().gameRenderer.camera.rotation.conjugate(Quaternionf()))
    }

    /**
     * Applies the camera offset and rotation to the given matrices.
     * This is useful for rendering things in world space, as it will negate the camera position and rotation.
     */
    fun setup(matrices: UMatrixStack) {
        rotation(matrices)
        offset(matrices)
    }
}