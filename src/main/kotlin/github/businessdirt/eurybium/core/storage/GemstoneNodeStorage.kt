package github.businessdirt.eurybium.core.storage

import com.google.gson.reflect.TypeToken
import github.businessdirt.eurybium.core.storage.adapters.BlockPosAdapter
import github.businessdirt.eurybium.core.storage.adapters.IdentifierAdapter
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

typealias GemstoneNode = Map<Identifier, Set<BlockPos>>

class GemstoneNodeStorage(type: String) : Storage<MutableList<GemstoneNode>>(
    "gemstonenodes/$type.json",
    object : TypeToken<MutableList<GemstoneNode>>() {},
    listOf(BlockPosAdapter(), IdentifierAdapter())
) {

    val size: Int get() = data!!.size

    init {
        if (data == null) data = mutableListOf()
    }

    fun add(node: GemstoneNode) {
        data!!.add(node)
    }

    fun addAll(nodes: List<GemstoneNode>) {
        data!!.addAll(nodes)
    }

    fun set(index: Int, node: GemstoneNode) {
        data!![index] = node
    }

    fun setAll(nodes: List<GemstoneNode>) {
        data = nodes.toMutableList()
    }

    fun get(index: Int): GemstoneNode = data!![index]
    fun getAll(): List<GemstoneNode> = data!!

    /**
     * Gets the node the passed [BlockPos] belongs to.
     * If it is not in a node, null will be returned
     */
    fun getNode(position: BlockPos): GemstoneNode? {
        data!!.forEach { node ->
            for ((_, blockPos) in node) {
                if (blockPos == position) return node
            }
        }

        return null;
    }
}