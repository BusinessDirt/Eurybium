package github.businessdirt.eurybium.core.storage.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import github.businessdirt.eurybium.data.model.IslandType
import java.lang.reflect.Type

class IslandTypeAdapter : AbstractAdapter<IslandType>(IslandType::class.java) {

    override fun serialize(
        src: IslandType,
        type: Type,
        context: JsonSerializationContext,
    ): JsonElement = JsonPrimitive(src.name)

    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext,
    ): IslandType = IslandType.entries.find { it.name == json.asJsonPrimitive.asString } ?: IslandType.UNKNOWN
}