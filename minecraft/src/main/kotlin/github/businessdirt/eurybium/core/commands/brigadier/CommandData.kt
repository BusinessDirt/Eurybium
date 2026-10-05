package github.businessdirt.eurybium.core.commands.brigadier

import github.businessdirt.eurybium.core.commands.CommandCategory
import java.util.function.Function

interface CommandData {
    val name: String
    val description: String
    val category: CommandCategory
    val aliases: List<String>

    fun allNames(): List<String> {
        val copy = mutableListOf(name)
        copy.addAll(aliases)
        return copy
    }

    fun checkDescriptionAndCategory() {
        if (description.isEmpty() && !CommandCategory.developmentCategories.contains(category)) throw RuntimeException(
            "The command '$name' has no required description!"
        )
    }

    fun hasUniqueName(builders: List<CommandData>) {
        val names = allNames()
        require(names.distinct().size == names.size) { "The command '$name' repeats a name or alias!" }
        val existingNames = builders.flatMap { it.allNames() }.toSet()
        val duplicate = names.firstOrNull { it in existingNames }
        require(duplicate == null) { "The command '$duplicate' has already been registered!" }
    }

    fun addBuilder(builders: MutableList<CommandData>) {
        val comparator =
            Comparator.comparing(Function { obj: CommandData -> obj.category })
                .thenComparing { obj: CommandData -> obj.name }

        for (i in builders.indices) {
            val command = builders[i]
            val comparison = comparator.compare(this, command)

            if (comparison < 0) {
                builders.add(i, this)
                return
            }
        }

        builders.add(this)
    }
}
