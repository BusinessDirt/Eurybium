package github.businessdirt.eurybium.minecraft

import gg.essential.universal.UChat
import github.businessdirt.eurybium.api.ClientPlatform
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

internal class FabricClientPlatform : ClientPlatform {
    private val logger = LoggerFactory.getLogger("Eurybium")
    override val minecraftVersion: String = FabricLoader.getInstance()
        .getModContainer("minecraft").orElseThrow().metadata.version.friendlyString

    override fun showChatMessage(message: String) = UChat.chat(message)
    override fun logInfo(message: String) = logger.info(message)
}
