package github.businessdirt.eurybium.core.storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import github.businessdirt.eurybium.core.json.BaseGsonBuilder
import github.businessdirt.eurybium.utils.Reference
import java.io.File
import java.io.FileReader
import java.io.FileWriter

abstract class Storage<T>(path: String, val type: TypeToken<T>) {

    val file: File = File("./config/" + Reference.MOD_ID + "/" + path)
    val gson: Gson = BaseGsonBuilder.gson().create()
    var data: T? = load()

    /**
     * Save a single object to the file.
     */
    fun save() {
        file.parentFile?.mkdirs()
        FileWriter(file).use { writer ->
            gson.toJson(data, writer)
        }
    }

    /**
     * Load a single object from the file, or null if file does not exist.
     */
    private fun load(): T? {
        if (!file.exists()) return null
        return try {
            FileReader(file).use { reader ->
                gson.fromJson(reader, type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}