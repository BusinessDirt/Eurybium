package github.businessdirt.eurybium.core.utils.files

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.concurrency.BackgroundTasks
import kotlinx.coroutines.Job
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration

/** Removes expired backups while retaining files from the three newest modification dates. */
object FileCleanup {

    /** Cleans [root] on an IO thread; [expiryDuration] must be finite and non-negative. */
    fun deleteExpiredFiles(root: File, expiryDuration: Duration): Job {
        validateExpiry(expiryDuration)
        return BackgroundTasks.launchIO("deleteExpiredFiles") { cleanup(root, expiryDuration) }
    }

    private data class Entry(val path: Path, val modified: Instant, val date: LocalDate, val empty: Boolean)

    /** Blocking cleanup implementation with an injectable instant for deterministic retention tests. */
    internal fun cleanup(root: File, expiryDuration: Duration, now: Instant = Instant.now()) {
        validateExpiry(expiryDuration)
        if (!root.exists()) return

        val files = mutableListOf<Entry>()
        val directories = mutableListOf<Path>()
        val zone = ZoneId.systemDefault()

        // NIO traversal does not follow symbolic links. A link in the backup tree must never
        // cause cleanup to visit and delete files in an unrelated directory.
        Files.walkFileTree(root.toPath(), object : SimpleFileVisitor<Path>() {
            override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
                if (attributes.isRegularFile) {
                    val modified = attributes.lastModifiedTime().toInstant()
                    files.add(Entry(path, modified, modified.atZone(zone).toLocalDate(), attributes.size() == 0L))
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(path: Path, failure: IOException): FileVisitResult {
                EurybiumMod.logger.warn("Unable to inspect backup '$path'", failure)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(path: Path, failure: IOException?): FileVisitResult {
                if (failure == null) directories.add(path)
                return FileVisitResult.CONTINUE
            }
        })

        val retainedDates = files.asSequence().map { it.date }.distinct().sortedDescending().take(3).toSet()
        val cutoff = now.minusMillis(expiryDuration.inWholeMilliseconds)

        for (entry in files) {
            if (entry.date !in retainedDates && (entry.empty || entry.modified.isBefore(cutoff))) {
                entry.path.toFile().deleteWithError()
            }
        }

        // Directories were collected after their children, so newly empty parents can be removed too.
        for (directory in directories) {
            try {
                val empty = Files.newDirectoryStream(directory).use { !it.iterator().hasNext() }
                if (empty) directory.toFile().deleteWithError()
            } catch (failure: IOException) {
                EurybiumMod.logger.warn("Unable to inspect backup directory '$directory'", failure)
            }
        }
    }

    private fun validateExpiry(expiryDuration: Duration) {
        require(expiryDuration.isFinite() && !expiryDuration.isNegative()) {
            "Expiry duration must be finite and non-negative"
        }
    }

    /** Attempts deletion and logs a failure instead of throwing; returns whether deletion succeeded. */
    fun File.deleteWithError(): Boolean = delete().also { deleted ->
        if (!deleted) EurybiumMod.logger.error("Failed to delete file '$absolutePath'")
    }
}
