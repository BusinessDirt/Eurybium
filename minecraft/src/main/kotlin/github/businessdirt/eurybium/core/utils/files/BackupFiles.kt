package github.businessdirt.eurybium.core.utils.files

import github.businessdirt.eurybium.generated.BuildInfo
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Constructs dated JSON backup paths without creating directories or writing files. */
object BackupFiles {

    private val directoryFormat = DateTimeFormatter.ofPattern("yyyy/MM")

    /** Returns `backup/yyyy/MM/yyyy-MM-dd-version-name.json` beside [file], using the local calendar date. */
    fun getBackupFile(file: File): File {
        val date = LocalDate.now()
        val directory = File(file.parentFile, "backup/${date.format(directoryFormat)}")

        return File(directory, "$date-${BuildInfo.VERSION}-${file.nameWithoutExtension}.json")
    }
}
