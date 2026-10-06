package github.businessdirt.eurybium.core.utils.files

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Path
import kotlin.test.*

class RecoverableTextFileTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun `saves and replaces UTF-8 text without temporary leftovers`() {
        val file = directory.resolve("config.json").toFile()
        val storage = RecoverableTextFile(file)
        storage.save("first")
        storage.save("second 🌍")
        assertEquals("second 🌍", storage.load())
        assertFalse(directory.resolve("config.json.tmp").toFile().exists())
        assertFalse(directory.resolve("config.json.bak").toFile().exists())
    }

    @Test
    fun `missing primary can load its recovery backup`() {
        directory.resolve("config.json.bak").toFile().writeText("recovered")
        assertEquals("recovered", RecoverableTextFile(directory.resolve("config.json").toFile()).load())
    }

    @Test
    fun `failed staging write never restores a stale backup over an intact primary`() {
        val file = directory.resolve("config.json").toFile().apply { writeText("current") }
        directory.resolve("config.json.bak").toFile().writeText("stale")
        directory.resolve("config.json.tmp").toFile().mkdir()
        assertFailsWith<IOException> { RecoverableTextFile(file).save("new") }
        assertEquals("current", file.readText())
    }

    @Test
    fun `missing primary and missing backup report the read failure`() {
        assertFailsWith<IOException> { RecoverableTextFile(directory.resolve("missing").toFile()).load() }
    }
}
