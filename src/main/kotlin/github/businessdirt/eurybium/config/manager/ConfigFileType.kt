package github.businessdirt.eurybium.config.manager

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.config.EurybiumConfig
import github.businessdirt.eurybium.utils.files.FileUtils
import java.io.File
import kotlin.reflect.KMutableProperty0

enum class ConfigFileType(val fileName: String, val clazz: Class<*>, val property: KMutableProperty0<*>) {
    CONFIG("config", EurybiumConfig::class.java, EurybiumMod::config),
    //ROUTES("routes", OrderedWaypointsRoutes::class.java, SkyHanniMod::orderedWaypointsRoutesData),
    ;

    val file by lazy { File(ConfigManager.configDirectory, "$fileName.json") }
    val backupFile get() = FileUtils.getBackupFile(file)
}