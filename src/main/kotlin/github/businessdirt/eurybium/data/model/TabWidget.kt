package github.businessdirt.eurybium.data.model

import github.businessdirt.eurybium.events.TabWidgetUpdateEvent
import github.businessdirt.eurybium.utils.SkyBlockUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This class defines various widgets within the tab list, specifically focusing on the reading of the values.
 * Each enum value corresponds to a distinct widget in the tab list, ensuring no overlap between them.
 * The general info widget is broken up into multiple smaller ones.
 * The class facilitates access to the lines associated with each widget and triggers events when a widget undergoes changes or becomes invisible.
 */
enum class TabWidget(pattern: String) {
    PLAYER_LIST( // language=RegExp
        "(?:§.)*Players (?:§.)*\\(\\d+\\)"
    ),
    INFO( // language=RegExp
        "(?:§.)*Info"
    ),
    AREA( // language=RegExp
        "(?:§.)*(Area|Dungeon): (?:§.)*(?<island>.*)"
    ),
    SERVER( // language=RegExp
        "Server: (?:§.)*(?<serverid>.*)"
    ),
    GEMS( // language=RegExp
        "Gems: (?:§.)*(?<gems>.*)"
    ),
    FAIRY_SOULS( // language=RegExp
        "Fairy Souls: (?:§.)*(?<got>\\d+)(?:§.)*\\/(?:§.)*(?<max>\\d+)"
    ),
    PROFILE( // language=RegExp
        "(?:§.)+Profile: §r§a(?<profile>[\\w\\s]+[^ §]).*"
    ),
    SB_LEVEL( // language=RegExp
        "SB Level(?:§.)*: (?:§.)*\\[(?:§.)*(?<level>\\d+)(?:§.)*\\] (?:§.)*(?<xp>\\d+).*"
    ),
    BANK( // language=RegExp
        "Bank: (?:§.)*(?<amount>[^§]+)(?:(?:§.)* \\/ (?:§.)*(?<personal>.*))?"
    ),
    INTEREST( // language=RegExp
        "Interest: (?:§.)*(?<time>[^§]+)(?:§.)*( \\((?<amount>[^)]+)\\))?"
    ),
    SOULFLOW( // language=RegExp
        "Soulflow: (?:§.)*(?<amount>.*)"
    ),
    PET( // language=RegExp
        "(?:§.)*Pet:"
    ),
    PET_TRAINING( // language=RegExp
        "(?:§.)*Pet Training:"
    ),
    PET_SITTER( // language=RegExp
        "Kat: .*"
    ),
    FIRE_SALE( // language=RegExp
        "(?:§.)*Fire Sales: .*"
    ),
    ELECTION( // language=RegExp
        "(?:§.)*Election: (?:§.)*(?<time>.*)"
    ),
    EVENT( // language=RegExp
        "(?:§.)*Event: (?<color>(?:§.)*)(?<event>.*)"
    ),
    SKILLS( // language=RegExp
        "(?:§.)*Skills: ?(?:§.)*(?<avg>[\\d.]*)"
    ),
    STATS( // language=RegExp
        "(?:§.)*Stats:"
    ),
    GUESTS( // language=RegExp
        "(?:§.)*Guests (?:§.)*.*"
    ),
    COOP( // language=RegExp
        "(?:§.)*Coop (?:§.)*.*"
    ),
    ISLAND( // language=RegExp
        "(?:§.)*Island"
    ),
    MINION( // language=RegExp
        "(?:§.)*Minions: (?:§.)*(?<used>\\d+)(?:§.)*/(?:§.)*(?<max>\\d+)"
    ),
    JERRY_ISLAND_CLOSING( // language=RegExp
        "Island closes in: (?:§.)*(?<time>.*)"
    ),
    NORTH_STARS( // language=RegExp
        "North Stars: (?:§.)*(?<amount>\\d+)"
    ),
    COLLECTION( // language=RegExp
        "(?:§.)*Collection:"
    ),
    JACOB_CONTEST( // language=RegExp
        "(?:§.)*Jacob's Contest:.*"
    ),
    SLAYER( // language=RegExp
        "(?:§.)*Slayer:"
    ),
    DAILY_QUESTS( // language=RegExp
        "(?:§.)*Daily Quests:"
    ),
    ACTIVE_EFFECTS( // language=RegExp
        "(?:§.)*Active Effects: (?:§.)*\\((?<amount>\\d+)\\)"
    ),
    BESTIARY( // language=RegExp
        "(?:§.)*Bestiary:"
    ),
    ESSENCE( // language=RegExp
        "(?:§.)*Essence:.*"
    ),
    FORGE( // language=RegExp
        "(?:§.)*Forges:"
    ),
    TIMERS( // language=RegExp
        "(?:§.)*Timers:"
    ),
    DUNGEON_STATS( // language=RegExp
        "Opened Rooms: (?:§.)*(?<opend>\\d+)"
    ),
    PARTY( // language=RegExp
        "(?:§.)*Party:.*"
    ),
    TRAPPER( // language=RegExp
        "(?:§.)*Trapper:"
    ),
    COMMISSIONS( // language=RegExp
        "(?:§.)*Commissions:"
    ),
    POWDER( // language=RegExp
        "(?:§.)*Powders:"
    ),
    CRYSTAL( // language=RegExp
        "(?:§.)*Crystals:"
    ),
    UNCLAIMED_CHESTS( // language=RegExp
        "Unclaimed chests: (?:§.)*(?<amount>\\d+)"
    ),
    RAIN( // language=RegExp
        "(?<type>Thunder|Rain): (?:§.)*(?<time>.*)"
    ),
    BROODMOTHER( // language=RegExp
        "Broodmother: (?:§.)*(?<stage>.*)"
    ),
    EYES_PLACED( // language=RegExp
        "Eyes placed: (?:§.)*(?<amount>\\d).*|(?:§.)*Dragon spawned!|(?:§.)*Egg respawning!"
    ),
    PROTECTOR( // language=RegExp
        "Protector: (?:§.)*(?<time>.*)"
    ),
    DRAGON( // language=RegExp
        "(?:§.)*Dragon: (?:§.)*\\((?<type>[^)]*)\\)"
    ),
    VOLCANO( // language=RegExp
        "Volcano: (?:§.)*(?<time>.*)"
    ),
    REPUTATION( // language=RegExp
        "(?:§.)*(?<faction>Barbarian|Mage) Reputation:"
    ),
    FACTION_QUESTS( // language=RegExp
        "(?:§.)*Faction Quests:"
    ),
    TROPHY_FISH( // language=RegExp
        "(?:§.)*Trophy Fish:"
    ),
    RIFT_INFO( // language=RegExp
        "(?:§.)*Good to know:"
    ),
    RIFT_SHEN( // language=RegExp
        "(?:§.)*Shen: (?:§.)*\\((?<time>[^)])\\)"
    ),
    RIFT_BARRY( // language=RegExp
        "(?:§.)*Advertisement:"
    ),
    COMPOSTER( // language=RegExp
        "(?:§.)*Composter:"
    ),
    GARDEN_LEVEL( // language=RegExp
        "Garden Level: (?:§.)*(?<level>.*)"
    ),
    COPPER( // language=RegExp
        "Copper: (?:§.)*(?<amount>\\d+)"
    ),
    PESTS( // language=RegExp
        "(?:§.)*Pests:"
    ),
    PEST_TRAPS( // language=RegExp
        "(?:§.)*Pest Traps: (?:§.)*(?<count>\\d+)\\/(?<max>\\d+)"
    ),
    FULL_TRAPS( // language=RegExp
        "(?:§.)*Full Traps: (?:§.)*(?:None|§r§a(?<traps>#\\d(?:§r§7, §r§a#\\d(?:§r§7, §r§a#\\d)?)?))"
    ),
    NO_BAIT( // language=RegExp
        "(?:§.)*No Bait: (?:§.)*(?:None|§r§c(?<traps>#\\d(?:§r§7, §r§c#\\d(?:§r§7, §r§c#\\d)?)?))"
    ),
    VISITORS( // language=RegExp
        "(?:§.)*Visitors: (?:§.)*\\((?<count>\\d+)\\)"
    ),
    CROP_MILESTONE( // language=RegExp
        "(?:§.)*Crop Milestones:"
    ),
    PRIVATE_ISLAND_CRYSTALS( // language=RegExp
        "Crystals: (?:§.)*(?<count>\\d+)"
    ),
    OLD_PET_SITTER( // language=RegExp
        "Pet Sitter:.*"
    ),
    DUNGEON_HUB_PROGRESS( // language=RegExp
        "(?:§.)*Dungeons:"
    ),
    DUNGEON_PUZZLE( // language=RegExp
        "(?:§.)*Puzzles: (?:§.)*\\((?<amount>\\d+)\\)"
    ),
    DUNGEON_PARTY( // language=RegExp
        "(?:§.)*Party (?:§.)*\\(\\d+\\)"
    ),
    DUNGEON_PLAYER_STATS( // language=RegExp
        "(?:§.)*Player Stats"
    ),
    DUNGEON_SKILLS_AND_STATS( // language=RegExp
        "(?:§.)*Skills: (?:§.)*\\w+ \\d+: (?:§.)*[\\d.]+%"
    ),
    DUNGEON_ACCOUNT_INFO_LINE( // language=RegExp
        "(?:§.)*Account Info"
    ),
    DUNGEON_STATS_LINE( // language=RegExp
        "(?:§.)*Dungeon Stats"
    ),
    FROZEN_CORPSES( // language=RegExp
        "§b§lFrozen Corpses:"
    ),
    SCRAP( // language=RegExp
        "Scrap: (?:§.)*(?<amount>\\d)(?:§.)*/(?:§.)*\\d"
    ),
    EVENT_TRACKERS( // language=RegExp
        "§e§lEvent Trackers:"
    ),
    AGATHA_CONTEST( // language=RegExp
        "(?:§.)*Agatha's Contest:.*"
    ),
    MOONGLADE_BEACON( // language=RegExp
        "(?:§.)*Moonglade Beacon: §r§b(?<stacks>\\d+) Stacks?"
    ),
    SALTS( // language=RegExp
        "(?:§.)*Salts:"
    ),
    FOREST_WHISPERS( // language=RegExp
        "(?:§.)*Forest Whispers: (?:§.)*(?<amount>.*)"
    ),
    SHARD_TRAPS( // language=RegExp
        "(?:§.)*Shard Traps"
    ),
    STARBORN_TEMPLE( // language=RegExp
        "§9§lStarborn Temple:"
    ),
    ;

    /**
     * The pattern for the first line of the widget
     */
    val pattern: Pattern = Pattern.compile(pattern)

    /**
     * The current active information from tab list. When the widget isn't visible, it will be empty
     */
    var lines: List<String> = emptyList()

    /**
     * Both are inclusive
     */
    var boundary = -1 to -1
            private set

    /**
     * If this widget is currently visible in the tab list
     */
    var isActive = false
        private set

    /**
     * Internal value for the checking to set `isActive`
     */
    private var gotChecked = false

    inline fun <T> matchFirstLine(consumer: Matcher.() -> T) =
        if (isActive) {
            val matcher = this.pattern.matcher(lines.first())
            if (matcher.find()) consumer.invoke(matcher) else null
        } else null

    private fun postNewEvent(newLines: List<String>) {
        // Prevent Post if lines are equal
        if (newLines == this.lines) return
        this.lines = newLines
        isActive = true
        TabWidgetUpdateEvent(this, newLines).post()
    }

    private fun postClearEvent() {
        lines = emptyList()
        TabWidgetUpdateEvent(this, lines).post()
    }

    private fun updateIsActive() {
        if (this.isActive == this.gotChecked) return
        isActive = gotChecked
        if (!gotChecked) this.postClearEvent()
    }

    companion object {
        private val separatorIndices = mutableListOf<Pair<Int, TabWidget?>>()

        init {
            entries.forEach { it.pattern }
        }

        /**
         * Updates the positions of all [TabWidget] instances in the tab list.
         * This method is invoked immediately after a [TabListUpdateEvent][github.businessdirt.eurybium.events.TabListUpdateEvent] is fired.
         * It scans the provided tab list and records the indices of each [TabWidget].
         * @param tabList the current tab list captured by the event
         */
        fun onTabListUpdate(tabList: List<String>) {
            if (!SkyBlockUtils.inSkyblock()) {
                if (separatorIndices.isNotEmpty()) {
                    separatorIndices.forEach { it.second?.updateIsActive() }
                    separatorIndices.clear()
                }
            } else {
                separatorIndices.clear()
                val filteredTabList: List<String> = filterTabList(tabList)

                for ((index, line) in filteredTabList.withIndex()) {
                    val match = entries.firstOrNull { it.pattern.matcher(line).matches() }
                        ?: continue
                    separatorIndices.add(index to match)
                }

                separatorIndices.add(filteredTabList.size to null)

                separatorIndices.zipWithNext { (firstIndex, widget), (secondIndex, _) ->
                    widget?.boundary = firstIndex to secondIndex - 1
                    widget?.gotChecked = true
                    widget?.postNewEvent(tabList.subList(firstIndex, secondIndex).filter { it.isNotEmpty() })
                }

                entries.forEach { it.updateIsActive() }
                separatorIndices.forEach { it.second?.gotChecked = false }
            }
        }

        /**
         * Removes duplicate header entries from the provided tab list.
         * Headers are defined as the first entry of each column, located at indices
         * 0, 20, 40, 60, etc. If multiple columns have the same header, all subsequent
         * occurrences are removed while preserving the first one. All remaining elements
         * will be moved up accordingly.
         *
         * Example:
         * Input (indices shown for clarity):
         * - 0  : "Players (21)"   // first column header
         * - 20 : "Info"           // second column header
         * - 40 : "Players (21)"   // third column header (duplicate of first)
         * - 60 : "Stats"          // fourth column header
         *
         * Output:
         * - 0  : "Players (21)"
         * - 20 : "Info"
         * - 59 : "Stats"
         *
         * In this example, the duplicate header at index 40 ("Players (21)") is removed.
         * @param tabList the original tab list, expected to contain up to 80 entries
         * @return a new list with duplicate headers removed
         */
        private fun filterTabList(tabList: List<String>): List<String> {
            var playerListFound = false
            var infoFound = false

            val headers = generateSequence(0) { it + 20 }.take(4).map { it to tabList.getOrNull(it) }
            val removeIndices: MutableList<Int> = mutableListOf()

            for ((index, header) in headers) when {
                PLAYER_LIST.pattern.matcher(header.orEmpty()).matches() ->
                    if (playerListFound) removeIndices.add(index)
                    else playerListFound = true
                INFO.pattern.matcher(header.orEmpty()).matches() ->
                    if (infoFound) removeIndices.add(index)
                    else infoFound = true
            }


            return (0 until tabList.size)
                .filter { i -> i !in removeIndices }
                .map { index -> tabList[index] }
        }
    }
}
