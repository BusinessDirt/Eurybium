package github.businessdirt.eurybium.core.json.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType

object ListEnumSkippingTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (rawType == List::class.java && type.type is ParameterizedType) {
            val paramType = type.type as ParameterizedType
            val actualType = paramType.actualTypeArguments[0]
            if (actualType is Class<*> && actualType.isEnum) {
                @Suppress("UNCHECKED_CAST")
                return ListEnumSkippingTypeAdapter(actualType as Class<out Enum<*>>) as TypeAdapter<T>
            }
        }
        return null
    }
}

class ListEnumSkippingTypeAdapter<T : Enum<T>>(private val enumClass: Class<T>) : TypeAdapter<List<T>>() {
    override fun write(out: JsonWriter, value: List<T>?) {
        value ?: return
        out.beginArray()
        value.forEach { out.value(it.name) }
        out.endArray()
    }

    override fun read(reader: JsonReader): List<T> {
        val list = mutableListOf<T>()
        reader.beginArray()
        while (reader.hasNext()) {
            if (reader.peek() == JsonToken.NULL) {
                reader.skipValue()
                continue
            }
            val name = reader.nextString()
            enumClass.enumConstants.firstOrNull { it.name == name }?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }
}