package github.businessdirt.eurybium.api.events

import github.businessdirt.eurybium.core.utils.ReflectionUtils.fullyQualifiedName
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

class MethodNotPublicException(function: KFunction<*>) : RuntimeException("Method ${function.fullyQualifiedName} is not public")

class ParameterException(
    function: KFunction<*>,
    extra: String,
) : RuntimeException("Method ${function.fullyQualifiedName} $extra")

class InvalidConsumerException(
    function: KFunction<*>,
    cause: Throwable?
) : RuntimeException(
    "Method ${function.fullyQualifiedName} is not a valid consumer. ${function.internalExecutableCause(1)}",
    cause
)

class InvalidRunnableException(
    function: KFunction<*>,
    cause: Throwable?
) : RuntimeException(
    "Method ${function.fullyQualifiedName} is not a valid runnable. ${function.internalExecutableCause(0)}",
    cause
)

private fun KFunction<*>.internalExecutableCause(
    expectedSize: Int,
): String = when (valueParameters.size) {
    expectedSize -> "Unknown reason."
    else -> "Expected parameter count of $expectedSize but was ${this.valueParameters.size}"
}
