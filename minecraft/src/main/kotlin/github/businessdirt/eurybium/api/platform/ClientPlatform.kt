package github.businessdirt.eurybium.api.platform

/** Minecraft-free services. Calls are made on the client thread. */
interface ClientPlatform {
    val minecraftVersion: String

    /** Displays a local message; never sends chat to the server. */
    fun showChatMessage(message: String)
    fun logInfo(message: String)
}
