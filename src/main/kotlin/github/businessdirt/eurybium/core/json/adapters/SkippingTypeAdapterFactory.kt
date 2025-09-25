package github.businessdirt.eurybium.core.json.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import github.businessdirt.eurybium.EurybiumMod

object SkippingTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
        return SafeTypeAdapter(gson.getDelegateAdapter(this, type))
    }

    private class SafeTypeAdapter<T>(val parent: TypeAdapter<T>) : TypeAdapter<T>() {
        override fun write(writer: JsonWriter, value: T) {
            parent.write(writer, value)
        }

        override fun read(reader: JsonReader): T? {
            return try {
                parent.read(reader)
            } catch (e: Exception) {
                // TODO include path and value found (as string)
                EurybiumMod.logger.warn("Failed to read value from JSON, skipping", e)
                if (!reader.hasNext()) return null
                // reader skip value seems to have an infinite loop if you dont have another element
                reader.skipValue()

                null
            }
        }
    }
}