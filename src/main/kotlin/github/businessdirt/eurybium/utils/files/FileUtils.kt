package github.businessdirt.eurybium.utils.files

import github.businessdirt.eurybium.utils.Reference
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

        return File(directory, "$year-$month-$day-${Reference.MOD_VERSION}-$fileName.json")
    }
}