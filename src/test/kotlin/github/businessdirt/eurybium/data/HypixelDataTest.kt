package github.businessdirt.eurybium.data

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.function.Executable
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Stream

internal class HypixelDataTest {
    private fun patternMatchDynamicTestHelper(matches: Array<String>, pattern: Pattern): Stream<DynamicTest> {
        return Arrays.stream(matches).map { match ->
            DynamicTest.dynamicTest("Should match: $match") {
                Assertions.assertTrue(pattern.matcher(match).matches())
            }
        }
    }

    @TestFactory
    @DisplayName("Server name connection pattern should work")
    fun testServerNameConnectionPattern(): Stream<DynamicTest> {
        val matches = arrayOf("www.hypixel.net", "alpha.hypixel.net")
        return patternMatchDynamicTestHelper(matches, HypixelData.SERVER_NAME_CONNECTION_PATTERN)
    }

    @TestFactory
    @DisplayName("Island name pattern should work")
    fun testIslandNamePattern(): Stream<DynamicTest> {
        val matches = arrayOf("§b§lArea: §r§7Private Island", "§b§lDungeon: §r§7Catacombs")
        return patternMatchDynamicTestHelper(matches, HypixelData.ISLAND_NAME_PATTERN)
    }

    @TestFactory
    @DisplayName("Scoreboard title pattern should work")
    fun testScoreboardTitlePattern(): Stream<DynamicTest> {
        val matches = arrayOf("SKYBLOCK", "SKYBLOCK GUEST", "SKYBLOCK CO-OP")
        return patternMatchDynamicTestHelper(matches, HypixelData.SCOREBOARD_TITLE_PATTERN)
    }

    @TestFactory
    @DisplayName("Skyblock area pattern should work")
    fun testSkyblockAreaPattern(): Stream<DynamicTest> {
        val matches = arrayOf("⏣ Village", "ф Wizard Tower")
        return patternMatchDynamicTestHelper(matches, HypixelData.SKYBLOCK_AREA_PATTERN)
    }
}