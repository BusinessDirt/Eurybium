package github.businessdirt.eurybium.core.json.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class SimpleArrayTypeAdapter<T>(val serializer: T.() -> Array<String>,
                             val deserializer: Array<String>.() -> T
) : TypeAdapter<T>() {
    override fun write(writer: JsonWriter, value: T) {
        val serialized = value.serializer()
        writer.beginArray()
        for (s in serialized) {
            writer.value(s)
        }
        writer.endArray()
    }

    override fun read(reader: JsonReader): T? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        val list = mutableListOf<String>()
        reader.beginArray()
        while (reader.hasNext()) { list.add(reader.nextString()) }
        reader.endArray()
        return list.toTypedArray().deserializer()
    }
}