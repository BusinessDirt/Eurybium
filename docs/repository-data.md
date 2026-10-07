# Repository data API

## Retrieval, caching, and updates

`RepoAPI` starts after mod initialization. It restores and validates `config/eurybium/repo/cache.json` on an IO worker, publishes it on Minecraft's client thread, and then checks the public `BusinessDirt/Eurybium-Data` master branch. Checks repeat hourly; `/eybrepo refresh` requests an immediate check, and `/eybrepo` shows the live revision/catalog counts and the last refresh failure, if any.

The GitHub commits endpoint supplies a commit SHA and ETag. All JSON is retrieved using that SHA, never separate moving-branch URLs. The required files are `patterns/chat.json` and `patterns/scoreboard.json`; `mining/routes.json` and `mining/nodes.json` are optional while the data repository is being populated. An absent optional file is an empty catalog. A present malformed file rejects the entire candidate. `locations/mineshafts.json` and development-world ZIPs are not consumed by this loader; shaft detection's location format still needs its own contract.

Downloads have a 25-second whole-response deadline and an 8 MiB body limit. The enclosing update task has a 3-minute deadline and supports interruption. Concurrent refresh requests coalesce into one running update. Conditional HTTP checks and unchanged commit IDs skip catalog downloads. The cache stores raw files, revision, ETag, and fetch timestamp together with atomic replacement and a recovery backup. Startup revalidates the source files instead of trusting cached objects. A failed download/validation retains the previous memory/disk snapshot. A disk-write failure is logged but still allows fresh validated data into memory.

One immutable snapshot is swapped before posting `RepoUpdateEvent`. Cache restoration and changed network revisions produce events; unchanged checks and failures do not. Pending publications are discarded after shutdown, and polling/download jobs are canceled. Event listeners run on the client thread and must move expensive work to a background task.

## Consumer API

```kotlin
private val abilityReady = RepoPattern("mining.ability.ready")

fun parseMessage(message: String) {
    val match = abilityReady.matchEntire(message) ?: return
    val ability = match.groups["ability"]?.value ?: return
    // Consume confirmed ability information here.
}

@HandleEvent
fun onRepoUpdate(event: RepoUpdateEvent) {
    // Invalidate feature-specific lookup caches for event.current.revision.
}
```

A `RepoPattern` handle resolves the current compiled regex each time it is used. Missing keys return null/false. Its optional fallback is compiled once. Use `resolve(snapshot)` when several related lookups need the same captured revision; retain a snapshot rather than repeatedly reading global state in background work.

```kotlin
val snapshot = RepoAPI.snapshot
val handle = RepoWaypointRoute("eurybium:JASP1")
val metadata = handle.resolve(snapshot) ?: return
val editableWaypoints = handle.waypoints(snapshot) ?: return
```

`RepoWaypointRoute` returns immutable metadata and creates a fresh editable waypoint copy for every conversion. Template-space routes require a supplied, verified `(RepoPosition) -> BlockPos` transform; conversion returns null without it. World-space routes use their original block positions. Automatic mining consumers must additionally validate metadata against the current island/shaft/region; manual loading chooses the route explicitly. `RepoAPI.snapshot.nodes` exposes immutable `RepoMiningNode` records for the future mining lookup index.

The existing ordered-route loader resolves `eurybium:` names from this snapshot and user names from saved routes. Namespaced IDs work without quotes, while saved names containing spaces retain quoted-string support. Suggestions include live repository route IDs. Save/erase reject the namespace regardless of case, preserving existing legacy saved entries in the file. Such entries are hidden from suggestions; rename them outside the namespace in the saved-route data to use them as user routes. Repo refreshes do not replace an active editable route or reset navigation.

## JSON contracts

Every file has `schemaVersion: 1`. Pattern IDs must be unique across both pattern catalogs. Example:

```json
{
  "schemaVersion": 1,
  "patterns": [
    { "id": "mining.ability.ready", "pattern": "Your (?<ability>.+) is now available!" }
  ]
}
```

This pattern is illustrative, not a verified Hypixel message. JSON strings use normal JSON escaping (for example, `\\d+` in a JSON string to encode regex `\d+`). Invalid regexes reject the candidate. Regexes are compiled once per snapshot rather than in chat handlers.

Route catalog:

```json
{
  "schemaVersion": 1,
  "routes": [{
    "id": "eurybium:JASP1",
    "island": "MINESHAFT",
    "mineshaft": "JASP1",
    "space": "WORLD",
    "points": [[10, 100, 20], [20, 100, 30]]
  }]
}
```

Node catalog:

```json
{
  "schemaVersion": 1,
  "nodes": [{
    "id": "jasp1-example-node",
    "island": "MINESHAFT",
    "mineshaft": "JASP1",
    "kind": "GEMSTONE",
    "material": "JASPER",
    "space": "WORLD",
    "blocks": [[10, 100, 20], [11, 100, 20]]
  }]
}
```

Coordinates above are illustrative. Publish only surveyed coordinates. Use island enum names, optional stable `region` IDs, and shaft IDs without the `eurybium:` prefix. `space` is required and is either `WORLD` or `TEMPLATE`; templates also require a nonempty `layout` ID and runtime placement resolution. Node kinds are `GEMSTONE`, `ORE`, and `MITHRIL`; materials use uppercase stable identifiers. Node block lists are deduplicated; route points retain order and duplicates.

Routes must use current shaft IDs from `MiningRouteIds.internalRouteId` or the three spawning constants (`SHAFT_SPAWN_MITHRIL`, `SHAFT_SPAWN_TUNGSTEN`, `SHAFT_SPAWN_GEMSTONES`). Shaft routes must identify the matching shaft and `MINESHAFT` island. Spawning routes must use `DWARVEN_MINES` and region `DWARVEN_BASE_CAMP`. Unknown built-in route IDs reject the candidate so new reserved names must first be agreed in mod code.

Initial validation limits: 20,000 records per catalog, 4,096 characters per regex, 10,000 points per route, 2,048 blocks per node, 250,000 node blocks total, and a 64 MiB encoded disk-cache bundle. Coordinates must be exact Int values and lists cannot be empty. Snapshot maps and nested coordinate lists are defensive, unmodifiable copies. Each new revision is accepted as a unit: one bad record prevents publication and preserves last-known-good data.

## Boundaries and verification

This layer retrieves, validates, caches, publishes, and provides typed access. It does not yet implement node matching/glow expansion, template placement detection, automatic shaft/spawn route loading, or mining notifications. Those consumers can use RepoUpdateEvent to invalidate their caches as described in the mining feature spec.

Tests cover current empty catalogs, pattern compilation, duplicate IDs, malformed/scoped/template data, immutable containers, independent route copies, same-commit downloads, HTTP failures, cache restoration/corruption/write failure, queued client-thread event delivery, shutdown, namespace protection, and namespaced command parsing. Tests use an injected transport and temporary cache directories; normal builds never contact GitHub.
