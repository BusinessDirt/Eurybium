package github.businessdirt.eurybium.events

import github.businessdirt.eurybium.api.events.EurybiumEvent
import github.businessdirt.eurybium.data.repo.RepoSnapshot

/**
 * Posted on the client thread after RepoAPI publishes a validated snapshot.
 * Cache restoration is also an update; unchanged checks and failed updates do not post this event.
 * Consumers may retain either immutable snapshot to compare data or invalidate their own caches.
 */
class RepoUpdateEvent(val previous: RepoSnapshot, val current: RepoSnapshot, val source: Source) : EurybiumEvent() {
    enum class Source { CACHE, NETWORK }
}
