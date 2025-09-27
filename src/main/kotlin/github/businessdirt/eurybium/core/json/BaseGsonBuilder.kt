package github.businessdirt.eurybium.core.json

import com.google.gson.GsonBuilder
import github.businessdirt.eurybium.config.features.mining.OrderedWaypointsConfig
import github.businessdirt.eurybium.core.json.adapters.EurybiumTypeAdapters
import github.businessdirt.eurybium.core.json.adapters.KotlinTypeAdapterFactory
import github.businessdirt.eurybium.core.json.adapters.ListEnumSkippingTypeAdapterFactory
import github.businessdirt.eurybium.core.json.adapters.SkippingTypeAdapterFactory
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.features.types.GemstoneType
import github.businessdirt.eurybium.features.types.MineshaftType
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.LegacyStringChromaColourTypeAdapter
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object BaseGsonBuilder {

    fun gson(): GsonBuilder = GsonBuilder().setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
        .registerTypeAdapterFactory(KotlinTypeAdapterFactory())
        .registerTypeAdapter(IslandType::class.java, EurybiumTypeAdapters.ISLAND_TYPE.nullSafe())
        .registerTypeAdapter(GemstoneType::class.java, EurybiumTypeAdapters.GEMSTONE_TYPE.nullSafe())
        .registerTypeAdapter(MineshaftType::class.java, EurybiumTypeAdapters.MINESHAFT_TYPE.nullSafe())
        .registerTypeAdapter(OrderedWaypointsConfig.RenderMode::class.java, EurybiumTypeAdapters.WAYPOINTS_RENDER_MODE.nullSafe())
        .registerTypeAdapter(Identifier::class.java, EurybiumTypeAdapters.IDENTIFIER.nullSafe())
        .registerTypeAdapter(BlockPos::class.java, EurybiumTypeAdapters.BLOCK_POS.nullSafe())
        .registerTypeAdapter(ChromaColour::class.java, LegacyStringChromaColourTypeAdapter(true).nullSafe())
        .enableComplexMapKeySerialization()

    fun lenientGson(): GsonBuilder = gson()
        .registerTypeAdapterFactory(SkippingTypeAdapterFactory)
        .registerTypeAdapterFactory(ListEnumSkippingTypeAdapterFactory)
}