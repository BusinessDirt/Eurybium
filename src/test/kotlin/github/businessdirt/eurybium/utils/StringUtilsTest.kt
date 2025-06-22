package github.businessdirt.eurybium.utils

import github.businessdirt.eurybium.utils.StringUtils.hasWhitespace
import github.businessdirt.eurybium.utils.StringUtils.optionalAn
import github.businessdirt.eurybium.utils.StringUtils.removeColor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.function.IntFunction
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class StringUtilsTest {
    @ParameterizedTest
    @MethodSource("testOptionalAnSource")
    @DisplayName("Adding an optional an or otherwise a should work")
    fun optionalAn(input: String, expected: String, message: String) {
        Assertions.assertEquals(expected, input.optionalAn(), message)
    }

    @ParameterizedTest
    @MethodSource("testHasWhitespaceSource")
    @DisplayName("Checking if a string has a whitespace should work")
    fun testHasWhitespace(testString: String, expected: Boolean, message: String) {
        Assertions.assertEquals(expected, testString.hasWhitespace(), message)
    }

    @TestFactory
    @DisplayName("Removing color codes from strings should work")
    fun testRemoveColor(): Stream<DynamicTest> {
        val data = arrayOf<Array<String>>(
            arrayOf(
                "§8- §c§aApex Dra§agon§a 486M§c❤",
                "- Apex Dragon 486M❤",
                "Triplicate inside string - should only remove one"
            ),
            arrayOf("§8- §c§6Flame Dr§6agon§a 460M§c❤", "- Flame Dragon 460M❤", "Duplicate inside string"),
            arrayOf("§aHello §aWorld", "Hello World", "No inside-word duplicate"),
            arrayOf("§bTes§bt", "Test", "Duplicate inside word"),
            arrayOf("", "", "Edge case: empty")
        )

        return Arrays.stream(data).map<DynamicTest> { testData ->
            DynamicTest.dynamicTest(testData[2]) {
                Assertions.assertEquals(testData[1], testData[0].removeColor())
            }
        }
    }

    companion object {
        fun testHasWhitespaceSource(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("test data1", true, "Single whitespace"),
                Arguments.of("test data 2", true, "Double whitespace"),
                Arguments.of("testdata3", false, "No whitespace"),
                Arguments.of("", false, "Edge case: empty string")
            )
        }

        fun testOptionalAnSource(): Stream<Arguments> {
            val anTests = "aeiouAEIOU".chars().mapToObj(IntFunction { c: Int ->
                Arguments.of(
                    Character.toString(c),
                    "an",
                    "String starting with '" + Character.toString(c) + "'"
                )
            })

            val aTests = "0123456789bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ".chars()
                .mapToObj(IntFunction { c: Int ->
                    Arguments.of(
                        Character.toString(c), "a", "String starting with '" + Character.toString(c) + "'"
                    )
                })

            return Stream.concat(anTests, aTests)
        }
    }
}