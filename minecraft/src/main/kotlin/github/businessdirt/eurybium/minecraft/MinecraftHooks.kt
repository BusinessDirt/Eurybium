package github.businessdirt.eurybium.minecraft

import github.businessdirt.eurybium.core.Eurybium

/** Bridges injected Minecraft hooks to shared logic, on the client thread. */
object MinecraftHooks {
    private var core: Eurybium? = null

    internal fun attach(core: Eurybium) {
        check(this.core == null) { "Eurybium is already initialized" }
        this.core = core
    }

    internal fun detach() {
        core = null
    }

    @JvmStatic
    fun onClientTick() {
        core?.onClientTick()
    }
}
