package github.businessdirt.eurybium.commands.brigadier

import com.mojang.brigadier.arguments.*

object BrigadierArguments {
    fun int(min: Int = Int.Companion.MIN_VALUE, max: Int = Int.Companion.MAX_VALUE): IntegerArgumentType =
        IntegerArgumentType.integer(min, max)

    fun long(min: Long = Long.Companion.MIN_VALUE, max: Long = Long.Companion.MAX_VALUE): LongArgumentType =
        LongArgumentType.longArg(min, max)

    fun double(min: Double = Double.Companion.MIN_VALUE, max: Double = Double.Companion.MAX_VALUE): DoubleArgumentType =
        DoubleArgumentType.doubleArg(min, max)

    fun float(min: Float = Float.Companion.MIN_VALUE, max: Float = Float.Companion.MAX_VALUE): FloatArgumentType =
        FloatArgumentType.floatArg(min, max)

    fun bool(): BoolArgumentType = BoolArgumentType.bool()

    fun string(): StringArgumentType = StringArgumentType.string()
    fun greedyString(): StringArgumentType = StringArgumentType.greedyString()
    fun word(): StringArgumentType = StringArgumentType.word()
}
