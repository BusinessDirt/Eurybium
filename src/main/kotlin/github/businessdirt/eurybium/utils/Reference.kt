package github.businessdirt.eurybium.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import github.businessdirt.eurybium.EurybiumMod
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.io.InputStreamReader

object Reference {
    const val MOD_ID: String = "eurybium"
    const val MOD_NAME: String = "Eurybium"
    val VERSION: String = version
    const val UNKNOWN_VERSION: String = "unknown"

    private val version: String
        get() {
            try {
                val urls = EurybiumMod::class.java.getClassLoader().getResources("fabric.mod.json")
                val gson = Gson()
                while (urls.hasMoreElements()) {
                    val url = urls.nextElement()
                    url.openStream().use { `is` ->
                        val jsonArray = gson.fromJson(InputStreamReader(`is`), JsonArray::class.java)
                        jsonArray.forEach { jsonElement ->
                            val json = jsonElement.asJsonObject
                            if (json.get("id").asString == MOD_ID) {
                                val version = json.get("version").asString
                                return version.ifEmpty { UNKNOWN_VERSION }
                            }
                        }
                    }
                }
                return UNKNOWN_VERSION
            } catch (ioe: IOException) {
                LogManager.getLogger(Reference::class.java)
                    .fatal("Failed while getting Eurybium version", ioe)
                return UNKNOWN_VERSION
            }
        }
}
