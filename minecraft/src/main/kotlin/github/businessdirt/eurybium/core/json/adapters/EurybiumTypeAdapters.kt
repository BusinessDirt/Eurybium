package github.businessdirt.eurybium.core.json.adapters

import com.google.gson.TypeAdapter
import github.businessdirt.eurybium.core.data.model.IslandType
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier

object EurybiumTypeAdapters {
    val ISLAND_TYPE: TypeAdapter<IslandType> = SimpleStringTypeAdapter.forEnum(IslandType.UNKNOWN)
    //val GEMSTONE_TYPE: TypeAdapter<GemstoneType> = SimpleStringTypeAdapter.forEnum(GemstoneType.UNKNOWN)
    //val MINESHAFT_TYPE: TypeAdapter<MineshaftType> = SimpleStringTypeAdapter.forEnum(MineshaftType.UNKNOWN)
    //val WAYPOINTS_RENDER_MODE: TypeAdapter<OrderedWaypointsConfig.RenderMode> = SimpleStringTypeAdapter.forEnum(OrderedWaypointsConfig.RenderMode.OUTLINE)

    val IDENTIFIER: TypeAdapter<Identifier> = SimpleStringTypeAdapter(
        { this.toString() },
        { Identifier.parse(this) }
    )

    val BLOCK_POS: TypeAdapter<BlockPos> = SimpleArrayTypeAdapter(
        { arrayOf(this.x.toString(), this.y.toString(), this.z.toString()) },
        { BlockPos(this[0].toInt(), this[1].toInt(), this[2].toInt()) },
    )
}
