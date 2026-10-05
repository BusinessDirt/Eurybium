package github.businessdirt.eurybium.core.events

import kotlin.test.BeforeTest
import kotlin.test.AfterTest

/** Isolate the singleton bus without adding reset operations to the production API. */
abstract class EventBusTestFixture {
    private val snapshots = mutableMapOf<String, Map<Any?, Any?>>()

    @BeforeTest
    fun isolateBus() {
        for (name in listOf("listeners", "handlers")) {
            val map = busMap(name)
            snapshots[name] = map.toMap()
            map.clear()
        }
    }

    @AfterTest
    fun restoreBus() {
        snapshots.forEach { (name, contents) -> busMap(name).apply { clear(); putAll(contents) } }
    }

    @Suppress("UNCHECKED_CAST")
    private fun busMap(name: String): MutableMap<Any?, Any?> = EurybiumEventBus::class.java.getDeclaredField(name)
        .apply { isAccessible = true }.get(EurybiumEventBus) as MutableMap<Any?, Any?>
}
