package github.businessdirt.eurybium.api.minecraft

import net.minecraft.util.Util

/** Actions delegated to Minecraft's operating-system integration. */
object PlatformActions {

    /** Opens [url] using the platform's URI handler, normally the user's browser. */
    fun openBrowser(url: String) = Util.getPlatform().openUri(url)
}
