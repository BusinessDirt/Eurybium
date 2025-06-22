package github.businessdirt.eurybium.config

import gg.essential.vigilance.Vigilant
import github.businessdirt.eurybium.config.features.About
import github.businessdirt.eurybium.config.features.dev.DevConfig
import github.businessdirt.eurybium.config.features.gui.GuiConfig
import github.businessdirt.eurybium.config.features.mining.MiningConfig
import github.businessdirt.eurybium.utils.Reference
import net.minecraft.client.MinecraftClient
import java.io.File

class EurybiumConfig : Vigilant(File("./config/" + Reference.MOD_ID + "/config.toml")) {
    val about: About = About()
    val gui: GuiConfig = GuiConfig()
    val mining: MiningConfig = MiningConfig()
    val dev: DevConfig = DevConfig()

    init {
        // Top
        this.category("About", about)
        this.category("GUI", gui)

        // Skills
        this.category("Mining", mining)

        // Bottom
        this.category("Dev", dev)
    }

    fun display() {
        MinecraftClient.getInstance().send { MinecraftClient.getInstance().setScreen(this.gui()) }
    }
}
