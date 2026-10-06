package github.businessdirt.eurybium.api.minecraft

import net.minecraft.util.Util

object PlatformActions {

    fun openBrowser(url: String) = Util.getPlatform().openUri(url)
}
