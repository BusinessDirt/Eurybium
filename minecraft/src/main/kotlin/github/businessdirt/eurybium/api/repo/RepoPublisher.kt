package github.businessdirt.eurybium.api.repo

import github.businessdirt.eurybium.data.repo.RepoSnapshot
import github.businessdirt.eurybium.events.RepoUpdateEvent

/** Queues publication on the client executor and drops queued results after shutdown. */
internal class RepoPublisher(
    private val execute: (() -> Unit) -> Unit,
    private val onUpdate: (RepoUpdateEvent) -> Unit,
) {
    @Volatile var snapshot: RepoSnapshot = RepoSnapshot.EMPTY
        private set
    @Volatile private var closed = false

    fun publish(candidate: RepoSnapshot, source: RepoUpdateEvent.Source) = execute {
        if (!closed && snapshot.revision != candidate.revision) {
            val previous = snapshot
            snapshot = candidate
            onUpdate(RepoUpdateEvent(previous, candidate, source))
        }
    }

    fun close() {
        closed = true
    }
}
