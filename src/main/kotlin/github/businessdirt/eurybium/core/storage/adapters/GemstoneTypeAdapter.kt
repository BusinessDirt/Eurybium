package github.businessdirt.eurybium.core.storage.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import github.businessdirt.eurybium.features.types.GemstoneType
import java.lang.reflect.Type

class GemstoneTypeAdapter : AbstractAdapter<GemstoneType>(GemstoneType::class.java) {
    override fun serialize(
        src: GemstoneType,
        type: Type,
        context: JsonSerializationContext,
    ): JsonElement = JsonPrimitive(src.name)

    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext,
    ): GemstoneType = GemstoneType.entries.find { it.name == json.asJsonPrimitive.asString } ?: throw RuntimeException("Unknown GemstoneType")
}