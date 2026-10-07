package github.businessdirt.eurybium.api.minecraft

import com.mojang.blaze3d.platform.ClipboardManager
import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.minecraft.chat.ChatAPI
import github.businessdirt.eurybium.core.concurrency.BackgroundTasks
import net.minecraft.util.Util

/** Actions delegated to Minecraft's operating-system integration. */
object PlatformActions {

    /** Opens [url] using the platform's URI handler, normally the user's browser. */
    fun openBrowser(url: String) = Util.getPlatform().openUri(url)

    fun copyToClipboard(text: String, step: Int = 0) {
        BackgroundTasks.launch("copyToClipboard") {
            try {
                ClipboardManager().setClipboard(getMinecraft().window, text)
            } catch (_: Exception) {
                if (step == 3) {
                    ChatAPI.userError("Error while trying to access the clipboard.")
                } else {
                    copyToClipboard(text, step + 1)
                }
            }
        }
    }

    fun readFromClipboard(step: Int = 0): String? {
        var shouldRetry = false
        val clipboard = ClipboardManager().getClipboard(getMinecraft().window) { _, _ -> shouldRetry = true }
        if (shouldRetry) {
            if (step == 3) {
                ChatAPI.userError("Cannot read from clipboard. Clipboard can not be accessed after 3 retries")
                return null
            } else {
                return readFromClipboard(step + 1)
            }
        }

        return clipboard
    }
}
