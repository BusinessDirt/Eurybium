package github.businessdirt.eurybium.api.repo

import github.businessdirt.eurybium.api.events.EventBusTestFixture
import github.businessdirt.eurybium.api.events.EurybiumEventBus
import github.businessdirt.eurybium.api.events.HandleEvent
import github.businessdirt.eurybium.data.repo.*
import github.businessdirt.eurybium.events.RepoUpdateEvent
import kotlin.test.*

class RepoPublisherTest : EventBusTestFixture() {
    private class Subscriber(private val publisher: () -> RepoPublisher) {
        val events = mutableListOf<RepoUpdateEvent>()
        @HandleEvent fun updated(event: RepoUpdateEvent) {
            assertSame(event.current, publisher().snapshot)
            events += event
        }
    }

    @Test
    fun `publication runs through executor and posts event after swapping snapshot`() {
        val queued = mutableListOf<() -> Unit>()
        lateinit var publisher: RepoPublisher
        val subscriber = Subscriber { publisher }
        EurybiumEventBus.register(subscriber)
        publisher = RepoPublisher(queued::add) { it.post() }
        val cached = RepoParser.parse(REVISION_A, 1, repoFiles())
        val fresh = RepoParser.parse(REVISION_B, 2, repoFiles())
        publisher.publish(cached, RepoUpdateEvent.Source.CACHE)
        publisher.publish(fresh, RepoUpdateEvent.Source.NETWORK)
        assertSame(RepoSnapshot.EMPTY, publisher.snapshot)
        assertTrue(subscriber.events.isEmpty())
        queued.forEach { it() }
        assertSame(fresh, publisher.snapshot)
        assertEquals(listOf(RepoUpdateEvent.Source.CACHE, RepoUpdateEvent.Source.NETWORK), subscriber.events.map { it.source })
        assertSame(cached, subscriber.events[1].previous)
    }

    @Test
    fun `unchanged revision does not post another update event`() {
        val events = mutableListOf<RepoUpdateEvent>()
        val publisher = RepoPublisher({ it() }, events::add)
        publisher.publish(RepoParser.parse(REVISION_A, 1, repoFiles()), RepoUpdateEvent.Source.NETWORK)
        publisher.publish(RepoParser.parse(REVISION_A, 2, repoFiles()), RepoUpdateEvent.Source.NETWORK)
        assertEquals(1, events.size)
    }

    @Test
    fun `shutdown drops queued publication and events`() {
        val queued = mutableListOf<() -> Unit>()
        val events = mutableListOf<RepoUpdateEvent>()
        val publisher = RepoPublisher(queued::add, events::add)
        publisher.publish(RepoParser.parse(REVISION_A, 1, repoFiles()), RepoUpdateEvent.Source.NETWORK)
        publisher.close()
        queued.single()()
        assertSame(RepoSnapshot.EMPTY, publisher.snapshot)
        assertTrue(events.isEmpty())
    }
}
