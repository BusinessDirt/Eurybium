package github.businessdirt.eurybium.core.utils.files

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class FileCleanupTest {

    @TempDir
    lateinit var directory: Path

    private val now = Instant.parse("2026-01-20T12:00:00Z")

    private fun file(root: Path, name: String, ageDays: Long, text: String = "backup"): Path {
        val path = root.resolve(name)
        Files.createDirectories(path.parent)
        Files.writeString(path, text)
        Files.setLastModifiedTime(path, FileTime.from(now.minusSeconds(ageDays * 86_400)))
        return path
    }

    @Test
    fun `cleanup retains the newest three dates and removes expired files and empty directories`() {
        val root = Files.createDirectory(directory.resolve("backups"))
        val retained = listOf(file(root, "a", 1), file(root, "b", 2), file(root, "c", 3), file(root, "same-day", 3))
        val expired = file(root, "nested/old", 20)
        val empty = file(root, "empty", 4, "")
        val recent = file(root, "recent", 5)
        FileCleanup.cleanup(root.toFile(), 7.days, now)
        retained.forEach { assertTrue(Files.exists(it)) }
        assertTrue(Files.exists(recent))
        assertFalse(Files.exists(expired))
        assertFalse(Files.exists(empty))
        assertFalse(Files.exists(root.resolve("nested")))
    }

    @Test
    fun `cleanup never traverses a directory symlink or a symlink root`() {
        val outside = Files.createDirectory(directory.resolve("outside"))
        val outsideFile = file(outside, "old", 100)
        val root = Files.createDirectory(directory.resolve("backups"))
        file(root, "a", 1); file(root, "b", 2); file(root, "c", 3)
        Files.createSymbolicLink(root.resolve("linked"), outside)
        FileCleanup.cleanup(root.toFile(), Duration.ZERO, now)
        assertTrue(Files.exists(outsideFile))
        val linkedRoot = Files.createSymbolicLink(directory.resolve("linked-root"), outside)
        FileCleanup.cleanup(linkedRoot.toFile(), Duration.ZERO, now)
        assertTrue(Files.exists(outsideFile))
    }

    @Test
    fun `missing directory is harmless and invalid expiry is rejected`() {
        val root = directory.resolve("missing").toFile()
        FileCleanup.cleanup(root, 1.days, now)
        assertFailsWith<IllegalArgumentException> { FileCleanup.cleanup(root, -1.days, now) }
        assertFailsWith<IllegalArgumentException> { FileCleanup.cleanup(root, Duration.INFINITE, now) }
    }
}
