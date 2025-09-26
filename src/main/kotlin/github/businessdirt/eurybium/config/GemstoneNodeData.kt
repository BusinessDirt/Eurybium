package github.businessdirt.eurybium.config

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.features.types.GemstoneType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

typealias GemstoneNode = MutableMap<Identifier, MutableSet<BlockPos>>

class GemstoneNodeData {

    @Expose
    var mineshaftNodes: MutableMap<String, MutableList<GemstoneNode>>? = null

    @Expose
    var crystalHollowNodes: MutableMap<GemstoneType, MutableList<GemstoneNode>>? = null
}