package github.businessdirt.eurybium.core.json.adapters

import com.google.gson.TypeAdapter
import com.google.gson.internal.bind.ArrayTypeAdapter
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.features.types.GemstoneType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object EurybiumTypeAdapters {
    val ISLAND_TYPE: TypeAdapter<IslandType> = SimpleStringTypeAdapter.forEnum<IslandType>(IslandType.UNKNOWN)
    val GEMSTONE_TYPE: TypeAdapter<GemstoneType> = SimpleStringTypeAdapter.forEnum<GemstoneType>(GemstoneType.UNKNOWN)

    val IDENTIFIER: TypeAdapter<Identifier> = SimpleStringTypeAdapter(
        { this.toString() },
        { Identifier.of(this) }
    )

    val BLOCK_POS: TypeAdapter<BlockPos> = SimpleArrayTypeAdapter(
        { arrayOf(this.x.toString(), this.y.toString(), this.z.toString()) },
        { BlockPos(this[0].toInt(), this[1].toInt(), this[2].toInt()) },
    )
}