package github.businessdirt.eurybium.core.storage.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import net.minecraft.util.Identifier
import java.lang.reflect.Type

class IdentifierAdapter : AbstractAdapter<Identifier>(Identifier::class.java) {
    override fun serialize(src: Identifier, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        JsonPrimitive(src.toString())

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Identifier =
        Identifier.of(json.asString)
}