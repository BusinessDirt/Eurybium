package github.businessdirt.eurybium.data.repo

internal const val REVISION_A = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
internal const val REVISION_B = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
internal const val EMPTY_PATTERNS = """{"schemaVersion":1,"patterns":[]}"""
internal const val JASPER_ROUTE = """{"schemaVersion":1,"routes":[{"id":"eurybium:JASP1","island":"MINESHAFT","mineshaft":"JASP1","space":"WORLD","points":[[10,100,20],[20,100,30]]}]}"""
internal const val JASPER_NODE = """{"schemaVersion":1,"nodes":[{"id":"jasper-one","island":"MINESHAFT","mineshaft":"JASP1","space":"WORLD","kind":"GEMSTONE","material":"JASPER","blocks":[[10,100,20],[11,100,20],[10,100,20]]}]}"""
internal fun repoFiles(): Map<String, String> = mapOf(
    "patterns/chat.json" to EMPTY_PATTERNS,
    "patterns/scoreboard.json" to EMPTY_PATTERNS,
)
