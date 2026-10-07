package github.businessdirt.eurybium.api.repo

import github.businessdirt.eurybium.data.repo.RepoSnapshot

/**
 * A stable key whose compiled regex follows repository updates without consumer re-registration.
 * Missing data returns null; an optional fallback is compiled once for use before repo data is available.
 * Example: `private val abilityReady = RepoPattern("mining.ability.ready")`.
 */
class RepoPattern(val id: String, fallback: String? = null) {
    private val fallbackRegex = fallback?.let(::Regex)

    fun resolve(snapshot: RepoSnapshot = RepoAPI.snapshot): Regex? = snapshot.patterns[id]?.regex ?: fallbackRegex

    fun matchEntire(text: String): MatchResult? = resolve()?.matchEntire(text)

    fun find(text: String): MatchResult? = resolve()?.find(text)

    fun matches(text: String): Boolean = resolve()?.matches(text) == true
}
