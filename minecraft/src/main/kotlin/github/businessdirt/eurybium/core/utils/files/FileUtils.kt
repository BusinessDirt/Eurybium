package github.businessdirt.eurybium.core.utils.files

import github.businessdirt.eurybium.generated.BuildInfo
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FileUtils {
    fun getBackupFile(file: File): File {
        val parent = file.parentFile
        val fileName = file.nameWithoutExtension
        val now = LocalDate.now()
        val year = now.format(DateTimeFormatter.ofPattern("yyyy"))
        val month = now.format(DateTimeFormatter.ofPattern("MM"))
        val day = now.format(DateTimeFormatter.ofPattern("dd"))

        val directory = File(parent, "backup/$year/$month")

        return File(directory, "$year-$month-$day-${BuildInfo.VERSION}-$fileName.json")
    }
}
