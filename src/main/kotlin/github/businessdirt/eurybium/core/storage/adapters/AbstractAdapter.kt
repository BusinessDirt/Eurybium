package github.businessdirt.eurybium.core.storage.adapters

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

abstract class AbstractAdapter<T>(val type: Type) : JsonSerializer<T>, JsonDeserializer<T>