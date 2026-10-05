package github.businessdirt.eurybium.core

import github.businessdirt.eurybium.api.platform.ClientPlatform
import github.businessdirt.eurybium.generated.BuildInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EurybiumTest {
    @Test
    fun `lifecycle and status work without Minecraft`() {
        val platform = RecordingPlatform()
        val core = Eurybium(platform)
        core.onClientTick()
        core.showStatus()
        assertTrue(platform.messages.isEmpty())
        core.start()
        core.start()
        core.onClientTick()
        core.showStatus()
        assertEquals(listOf("Eurybium ${BuildInfo.VERSION} | Minecraft test | Client ticks: 1"), platform.messages)
        core.stop()
        core.stop()
        core.onClientTick()
        core.showStatus()
        assertEquals(1, platform.messages.size)
        assertEquals(2, platform.logs.size)
        core.start()
        core.showStatus()
        assertTrue(platform.messages.last().endsWith("Client ticks: 0"))
    }

    private class RecordingPlatform : ClientPlatform {
        override val minecraftVersion = "test"
        val messages = mutableListOf<String>()
        val logs = mutableListOf<String>()
        override fun showChatMessage(message: String) { messages += message }
        override fun logInfo(message: String) { logs += message }
    }
}
