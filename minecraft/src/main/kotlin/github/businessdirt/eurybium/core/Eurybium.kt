package github.businessdirt.eurybium.core

import github.businessdirt.eurybium.api.ClientPlatform
import github.businessdirt.eurybium.generated.BuildInfo

/** Application lifecycle. Platform services are injected so it can be tested without a client. */
class Eurybium(private val platform: ClientPlatform) {
    private var started = false
    private var clientTicks = 0L

    fun start() {
        if (started) return
        started = true
        clientTicks = 0
        platform.logInfo("Eurybium ${BuildInfo.VERSION} started on Minecraft ${platform.minecraftVersion}")
    }

    fun onClientTick() {
        if (started) clientTicks++
    }

    fun showStatus() {
        if (!started) return
        platform.showChatMessage(
            "Eurybium ${BuildInfo.VERSION} | Minecraft ${platform.minecraftVersion} | Client ticks: $clientTicks"
        )
    }

    fun stop() {
        if (!started) return
        started = false
        platform.logInfo("Eurybium stopped")
    }
}
