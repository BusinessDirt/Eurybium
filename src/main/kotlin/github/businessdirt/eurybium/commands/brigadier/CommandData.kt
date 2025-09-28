package github.businessdirt.eurybium.commands.brigadier

import github.businessdirt.eurybium.commands.CommandCategory
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
        val allNames = this.allNames()
        builders.stream().filter { builder: CommandData -> allNames.contains(builder.name) }
            .forEach { builder: CommandData ->
                throw RuntimeException("The command '" + builder.name + "' has already been registered!")
            }
    }

    fun addBuilder(builders: MutableList<CommandData>) {
        val comparator =
            Comparator.comparing(Function { obj: CommandData -> obj.category })
                .thenComparing { obj: CommandData -> obj.name }

        for (i in builders.indices) {
            val command = builders[i]
            val comparison = comparator.compare(this, command)

            if (comparison < 0) {
                builders.add(i, command)
                return
            }
        }

        builders.add(this)
    }
}
