package github.businessdirt.eurybium.features.mining.glacitemineshaft

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.api.events.EventBusTestFixture
import github.businessdirt.eurybium.api.events.EurybiumEventBus
import github.businessdirt.eurybium.api.minecraft.TabListAPI
import github.businessdirt.eurybium.config.features.mining.glacitemineshaft.CorpseNotificationsConfig
import github.businessdirt.eurybium.data.model.MineshaftType
import github.businessdirt.eurybium.events.minecraft.ClientDisconnectEvent
import github.businessdirt.eurybium.events.skyblock.GlaciteMineshaftDetectionEvent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CorpseNotificationsTest : EventBusTestFixture() {
    private val pattern = Regex("""\s*(?:Frozen Corpses:)""")
    private val miningConfig get() = EurybiumMod.config.mining.glaciteMineshaft
    private lateinit var previous: CorpseNotificationsConfig

    @BeforeTest
    fun setup() {
        previous = miningConfig.corpseNotifications
        miningConfig.corpseNotifications = CorpseNotificationsConfig().apply { enabled = true }
        TabListAPI.update(emptyList(), null, null)
        EurybiumEventBus.register(CorpseNotifications)
        ClientDisconnectEvent().post()
    }

    @AfterTest
    fun cleanup() {
        ClientDisconnectEvent().post()
        miningConfig.corpseNotifications = previous
    }

    private fun widget(count: Int) = listOf("Frozen Corpses:") + List(count) { " Lapis: LOOTED" } + ""

    @Test
    fun `count includes looted and unlooted lapis but stops at the next widget`() {
        val lines = listOf(
            "Lapis: NOT LOOTED", "§bFrozen Corpses:§r", " §9Lapis: §aLOOTED",
            " Lapis: LOOTED", " Lapis: NOT LOOTED", " Lapis: NOT LOOTED", " Umber: LOOTED",
            " §r ", "Other widget:", "Lapis: NOT LOOTED",
        )
        assertEquals(4, CorpseNotifications.lapisCorpseCount(lines, pattern))
        assertNull(CorpseNotifications.lapisCorpseCount(listOf("Lapis: LOOTED"), pattern))
        assertNull(CorpseNotifications.lapisCorpseCount(lines, null))
    }

    @Test
    fun `wait for delayed widget and notify once at the exact threshold`() {
        miningConfig.corpseNotifications.corpseThresholds.ruby = 3f
        GlaciteMineshaftDetectionEvent(MineshaftType.RUBY_1).post()
        assertNull(CorpseNotifications.takeNotificationCount(emptyList(), pattern))
        assertNull(CorpseNotifications.takeNotificationCount(widget(2), pattern))
        assertEquals(3, CorpseNotifications.takeNotificationCount(widget(3), pattern))
        assertNull(CorpseNotifications.takeNotificationCount(widget(4), pattern))
    }

    @Test
    fun `zero still requires the widget and five disables the gemstone`() {
        miningConfig.corpseNotifications.corpseThresholds.ruby = 0f
        GlaciteMineshaftDetectionEvent(MineshaftType.RUBY_C).post()
        assertNull(CorpseNotifications.takeNotificationCount(emptyList(), pattern))
        assertEquals(0, CorpseNotifications.takeNotificationCount(widget(0), pattern))
        miningConfig.corpseNotifications.corpseThresholds.ruby = 5f
        GlaciteMineshaftDetectionEvent(MineshaftType.RUBY_2).post()
        assertNull(CorpseNotifications.takeNotificationCount(widget(5), pattern))
    }

    @Test
    fun `disabled notifications and disconnect cannot consume stale shaft data`() {
        miningConfig.corpseNotifications.corpseThresholds.topaz = 2f
        GlaciteMineshaftDetectionEvent(MineshaftType.TOPA_1).post()
        miningConfig.corpseNotifications.enabled = false
        assertNull(CorpseNotifications.takeNotificationCount(widget(4), pattern))
        ClientDisconnectEvent().post()
        miningConfig.corpseNotifications.enabled = true
        assertNull(CorpseNotifications.takeNotificationCount(widget(4), pattern))
        GlaciteMineshaftDetectionEvent(MineshaftType.TOPA_2).post()
        assertEquals(4, CorpseNotifications.takeNotificationCount(widget(4), pattern))
    }
}
