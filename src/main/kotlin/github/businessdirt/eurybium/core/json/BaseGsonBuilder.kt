package github.businessdirt.eurybium.core.json

import com.google.gson.GsonBuilder
import github.businessdirt.eurybium.core.json.adapters.EurybiumTypeAdapters
import github.businessdirt.eurybium.core.json.adapters.KotlinTypeAdapterFactory
import github.businessdirt.eurybium.core.json.adapters.ListEnumSkippingTypeAdapterFactory
import github.businessdirt.eurybium.core.json.adapters.SkippingTypeAdapterFactory
import github.businessdirt.eurybium.data.model.IslandType
import github.businessdirt.eurybium.features.types.GemstoneType
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
        .registerTypeAdapter(Identifier::class.java, EurybiumTypeAdapters.IDENTIFIER.nullSafe())
        .registerTypeAdapter(BlockPos::class.java, EurybiumTypeAdapters.BLOCK_POS.nullSafe())

    fun lenientGson(): GsonBuilder = gson()
        .registerTypeAdapterFactory(SkippingTypeAdapterFactory)
        .registerTypeAdapterFactory(ListEnumSkippingTypeAdapterFactory)
}