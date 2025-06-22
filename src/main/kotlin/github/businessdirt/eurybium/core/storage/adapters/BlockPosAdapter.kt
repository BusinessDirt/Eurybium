package github.businessdirt.eurybium.core.storage.adapters

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import net.minecraft.util.math.BlockPos
import java.lang.reflect.Type

class BlockPosAdapter : AbstractAdapter<BlockPos>(BlockPos::class.java) {

    override fun serialize(
        src: BlockPos,
        srcType: Type,
        context: JsonSerializationContext,
    ): JsonElement = JsonArray().apply {
        add(src.x)
        add(src.y)
        add(src.z)
    }

    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext,
    ): BlockPos {
        val array = json.asJsonArray
        return BlockPos(array[0].asInt, array[1].asInt, array[2].asInt)
    }
}