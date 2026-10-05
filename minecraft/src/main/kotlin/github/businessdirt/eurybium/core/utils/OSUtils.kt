package github.businessdirt.eurybium.core.utils

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import kotlinx.io.IOException
import net.minecraft.util.Util
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import kotlin.jvm.java
import kotlin.time.Duration

object OSUtils {

    fun openBrowser(url: String) = Util.getPlatform().openUri(url)

    private fun File.isExpired(
        expiryDuration: Duration,
        lastModifiedTime: SimpleTimeMark = lastModifiedTime()
    ): Boolean = lastModifiedTime.passedSince() > expiryDuration

    private fun File.lastModifiedTime(): SimpleTimeMark = try {
        val attributes = Files.readAttributes(toPath(), BasicFileAttributes::class.java)
        SimpleTimeMark(attributes.lastModifiedTime().toMillis())
    } catch (_: IOException) {
        EurybiumMod.logger.error("Error reading last modified attribute (file=$this, path=${this.absolutePath})")
        SimpleTimeMark.now()
    }

    private fun File.isEmptyFile() = length() == 0L
    private fun File.isEmptyDirectory() = listFiles()?.isEmpty() == true

    fun deleteExpiredFiles(root: File, expiryDuration: Duration) = ConcurrencyUtils.launch("deleteExpiredFiles") {
        val allFiles = root.walk().filter { it.isFile }.toList()
        val lastModified = allFiles.associateWith { file ->
            file.lastModifiedTime()
        }

        @Suppress("ConvertCallChainIntoSequence")
        val recentDays = lastModified.mapNotNull { it.value.toLocalDate() }
            .distinct()
            .sortedDescending()
            .take(3)
            .toSet()

        root.walkBottomUp().forEach { file ->
            when {
                file.isFile -> {
                    val lastModifiedTime = lastModified[file] ?: file.lastModifiedTime()
                    if (lastModifiedTime.toLocalDate() in recentDays) return@forEach

                    if (file.isEmptyFile() || file.isExpired(expiryDuration, lastModifiedTime)) {
                        file.deleteWithError()
                    }
                }

                file.isDirectory && file.isEmptyDirectory() -> {
                    file.deleteWithError()
                }
            }
        }
    }

    fun File.deleteWithError() {
        if (!this.delete()) EurybiumMod.logger.error("Failed to delete file (file=$this, path=${this.absolutePath})")
    }
}
