package github.businessdirt.eurybium.core.utils.files

import java.io.File
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

/**
 * UTF-8 text storage using a sibling temporary file and a recovery backup.
 *
 * Prefer atomic replacement; file systems without it use ordinary replacement while retaining
 * a backup until success. Methods block, so call them on an IO thread. One instance serializes
 * its operations; callers must not operate on the same path through multiple instances concurrently.
 */
class RecoverableTextFile(file: File) {

    private val file = file.absoluteFile
    private val backupFile = File(this.file.parentFile, "${file.name}.bak")
    private val tempFile = File(this.file.parentFile, "${file.name}.tmp")

    /** Reads the primary file, falling back to its backup only on an IO failure. */
    @Synchronized
    @Throws(IOException::class)
    fun load(): String = try {
        file.readText()
    } catch (failure: IOException) {
        if (!backupFile.isFile) throw failure

        try {
            backupFile.readText()
        } catch (backupFailure: IOException) {
            backupFailure.addSuppressed(failure)
            throw backupFailure
        }
    }

    /**
     * Writes [content], retrying transient access-denied failures up to five times.
     *
     * A failure before replacement leaves the original intact. A backup is kept after failure
     * for [load] to recover if replacement on a non-atomic file system lost the primary file.
     * Parent directories must already exist.
     */
    @Synchronized
    @Throws(IOException::class)
    fun save(content: String) {
        try {
            for (attempt in 0..5) {
                try {
                    tempFile.writeText(content)
                    if (file.isFile) file.copyTo(backupFile, overwrite = true)

                    replacePrimary()
                    backupFile.delete()
                    return
                } catch (denied: AccessDeniedException) {
                    if (attempt == 5) throw denied

                    try {
                        Thread.sleep(50)
                    } catch (interrupted: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Interrupted while saving $file", interrupted)
                    }
                }
            }
        } finally {
            // Never restore over an intact primary: backup creation itself may have failed halfway.
            // A leftover backup is a read fallback, not authority to overwrite the current file.
            if (tempFile.isFile) tempFile.delete()
        }
    }

    private fun replacePrimary() {
        try {
            Files.move(tempFile.toPath(), file.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(tempFile.toPath(), file.toPath(), REPLACE_EXISTING)
        }
    }
}
