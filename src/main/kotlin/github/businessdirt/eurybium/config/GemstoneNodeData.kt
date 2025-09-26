package github.businessdirt.eurybium.config

import com.google.gson.annotations.Expose
import github.businessdirt.eurybium.core.rendering.GlowingBlock
import github.businessdirt.eurybium.features.types.GemstoneType

typealias GemstoneNode = MutableSet<GlowingBlock>

class GemstoneNodeData {

    @Expose
    var mineshaftNodes: MutableMap<String, MutableList<GemstoneNode>>? = null

    @Expose
    var crystalHollowNodes: MutableMap<GemstoneType, MutableList<GemstoneNode>>? = null
}