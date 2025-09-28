package github.businessdirt.eurybium.commands.brigadier

import com.mojang.brigadier.context.CommandContext

@JvmRecord
@Suppress("unused")
data class ArgContext(val context: CommandContext<*>) {
    fun <T> getArg(argument: BrigadierArgument<T>): T = context.getArgument<T>(argument.argumentName, argument.clazz)

    fun <T> get(argument: BrigadierArgument<T>): T? = getArg(argument)

    operator fun <T> invoke(argument: BrigadierArgument<T>): T = getArg(argument)

    inline fun <reified T> getArgByName(name: String): T = context.getArgument(name, T::class.java)
}
