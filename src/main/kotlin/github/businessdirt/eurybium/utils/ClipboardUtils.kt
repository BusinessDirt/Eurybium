package github.businessdirt.eurybium.utils

import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.utils.concurrent.Coroutine
import net.minecraft.client.util.Clipboard

object ClipboardUtils {
    fun copyToClipboard(text: String, step: Int = 0) {
        Coroutine.launch("copyToClipboard") {
            try {
                Clipboard().setClipboard(getMinecraft().window.handle, text)
            } catch (e: Exception) {
                if (step == 3) {
                    EurybiumMod.logger.error("Error while trying to access the clipboard. ${e.stackTraceToString()}")
                } else {
                    copyToClipboard(text, step + 1)
                }
            }
        }
    }

    fun readFromClipboard(step: Int = 0): String? {
        var shouldRetry = false
        val clipboard = net.minecraft.client.util.Clipboard().getClipboard(0) { _, _ -> shouldRetry = true }
        if (shouldRetry) {
            if (step == 3) {
                EurybiumMod.logger.error("Cannot read from clipboard. Clipboard can not be accessed after 3 retries")
                return null
            } else {
                return readFromClipboard(step + 1)
            }
        }
        return clipboard
    }
}