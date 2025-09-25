package github.businessdirt.eurybium.config.manager

import com.google.gson.Gson
import gg.essential.universal.UMinecraft.getMinecraft
import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.json.BaseGsonBuilder
import github.businessdirt.eurybium.core.types.SimpleTimeMark
import github.businessdirt.eurybium.utils.Reference
import github.businessdirt.eurybium.utils.files.StringFileHandler
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.concurrent.fixedRateTimer
import kotlin.jvm.javaClass

class ConfigManager {
    companion object {
        val gson: Gson = BaseGsonBuilder.gson().create()
        val configDirectory = File("config/${Reference.MOD_ID}")
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val configHolder = ConfigHolder()
    lateinit var processor: ConfigProcessor

    fun initialize() {
        configHolder.checkLoaded(logger)
        configDirectory.mkdirs()

        ConfigFileType.entries.forEach { fileType ->
            configHolder.set(fileType, firstLoadFile(fileType.file, fileType, fileType.clazz.getDeclaredConstructor().newInstance()))
        }

        fixedRateTimer(name = "${Reference.MOD_ID}-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            saveConfig(ConfigFileType.CONFIG, "auto-save-60s")
        }

        processor = ConfigProcessor()
        BuiltinMoulConfigGuis.addProcessors(processor)

        val driver = ConfigProcessorDriver(processor)
        driver.warnForPrivateFields = false
        driver.processConfig(EurybiumMod.config)
    }

    private fun firstLoadFile(file: File, fileType: ConfigFileType, defaultValue: Any): Any {
        val fileName = fileType.fileName
        logger.info("Trying to load $fileName from $file")
        var output: Any? = defaultValue

        if (file.exists()) {
            try {
                val text = StringFileHandler(file).load()
                val lenientGson = BaseGsonBuilder.lenientGson().create()

                logger.info("load-$fileName-now")
                output = lenientGson.fromJson(text, defaultValue.javaClass)
                logger.info("Loaded $fileName from file")

            } catch (e: Exception) {

                logger.error(e.stackTraceToString())
                val backupFile = file.resolveSibling("$fileName-${SimpleTimeMark.now().toMillis()}-backup.json")
                logger.error("Exception while reading $file. Will load blank $fileName and save backup to $backupFile")
                logger.error("Exception was $e")

                try {
                    file.copyTo(backupFile)
                } catch (e: Exception) {
                    logger.error("Could not create backup for $fileName file")
                    logger.error(e.stackTraceToString())
                }
            }
        }

        if (output == defaultValue) {
            logger.info("Setting $fileName to be blank as it did not exist. It will be saved once something is written to it")
        }

        if (output == null) {
            logger.info("Setting $fileName to be blank as it was null. It will be saved once something is written to it")
            output = defaultValue
        }

        return output
    }

    fun saveConfig(fileType: ConfigFileType, reason: String) {
        val json = configHolder[fileType] ?: error("Could not find json object for $fileType")
        saveFile(fileType.file, fileType.fileName, json, reason)
        saveFile(fileType.backupFile, fileType.fileName, json, reason)
    }

    private fun saveFile(file: File, fileName: String, json: Any, reason: String) {
        logger.info("saveConfig: $reason")
        try {
            logger.info("Saving $fileName file")
            file.parentFile.mkdirs()
            StringFileHandler(file).save(gson.toJson(json))
            logger.info("Saved $fileName file successfully")
        } catch (e: Exception) {
            logger.error("Could not save $fileName file to $file")
            logger.error(e.stackTraceToString())
        }
    }

    fun openConfigGui() {
        getMinecraft().send {
            IMinecraft.getInstance().openWrappedScreen(MoulConfigEditor(processor))
        }
    }
}

