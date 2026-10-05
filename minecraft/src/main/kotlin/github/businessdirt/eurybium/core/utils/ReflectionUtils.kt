package github.businessdirt.eurybium.core.utils

import github.businessdirt.eurybium.core.events.EurybiumEvent
import github.businessdirt.eurybium.core.events.InvalidConsumerException
import github.businessdirt.eurybium.core.events.InvalidRunnableException
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Consumer
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod

@OptIn(ExperimentalStdlibApi::class)
object ReflectionUtils {

    val KFunction<*>.fullyQualifiedName: String
        get() {
            val method = javaMethod
            val declaring = method?.declaringClass?.name ?: "Unknown"
            val params = parameters
                .filter { it.kind == KParameter.Kind.VALUE }
                .joinToString(prefix = "(", postfix = ")", separator = ", ") {
                    it.type.toString().substringAfterLast('.')
                }

            return "$declaring::$name$params"
        }


    /**
     * Creates a [Consumer] that invokes the given method on the provided owner.
     * The method must be public, non-static, and take exactly one argument.
     *
     * @param instance the object on which the method will be invoked.
     * @param function the function to be invoked.
     * @return a [Consumer] that, when called, invokes the specified method.
     * @throws InvalidConsumerException if the method is not a valid consumer (e.g., wrong number of arguments).
     */
    @Throws(InvalidConsumerException::class)
    fun createSingleParameterEventConsumer(
        instance: Any,
        function: KFunction<*>
    ): Consumer<EurybiumEvent> {
        try {
            val method = function.javaMethod ?: throw InvalidConsumerException(function, null)
            require(method.returnType == Void.TYPE) { "Event listeners must return Unit" }

            val baseLookup = MethodHandles.lookup()
            val privateLookup = MethodHandles.privateLookupIn(method.declaringClass, baseLookup)
            val handle = privateLookup.unreflect(method)
            val rawEventClass = method.parameterTypes[0]
            val site = LambdaMetafactory.metafactory(
                privateLookup,
                "accept",
                MethodType.methodType(Consumer::class.java, method.declaringClass),
                MethodType.methodType(Void.TYPE, Any::class.java),
                handle,
                MethodType.methodType(Void.TYPE, rawEventClass)
            )

            @Suppress("UNCHECKED_CAST")
            return site.target.invoke(instance) as Consumer<EurybiumEvent>
        } catch (e: Throwable) {
            throw InvalidConsumerException(function, e)
        }
    }

    /**
     * Creates a [Consumer] that invokes the given method on the provided owner.
     * The method must be public, non-static, and take no arguments.
     *
     * @param instance the object on which the method will be invoked.
     * @param function the function to be invoked.
     * @return a [Consumer] that, when called, invokes the specified method.
     * @throws InvalidRunnableException if the method is not a valid runnable (e.g., wrong number of arguments).
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(InvalidRunnableException::class)
    fun createZeroParameterEventConsumer(
        instance: Any,
        function: KFunction<*>
    ): Consumer<EurybiumEvent> {
        try {
            val method = function.javaMethod ?: throw InvalidRunnableException(function, null)
            require(method.returnType == Void.TYPE) { "Event listeners must return Unit" }
            require(method.parameterCount == 0) { "Zero-parameter listener must take no arguments" }

            val baseLookup = MethodHandles.lookup()
            val privateLookup = MethodHandles.privateLookupIn(method.declaringClass, baseLookup)
            val handle = privateLookup.unreflect(method)

            // Adapt the handle to accept and discard the event argument (Object/Any)
            // Transforms () -> void into (Object) -> void, retaining the target receiver binding
            val adaptedHandle = MethodHandles.dropArguments(
                handle,
                1, // Insert after receiver (param index 0 is receiver, 1 is the new unused event argument)
                Any::class.java
            )

            // Generate a direct Consumer site instead of an intermediary Runnable
            val site = LambdaMetafactory.metafactory(
                privateLookup,
                "accept",
                MethodType.methodType(Consumer::class.java, method.declaringClass),   // Factory: Consumer get(DeclaringClass)
                MethodType.methodType(Void.TYPE, Any::class.java),             // Interface: void accept(Object t)
                adaptedHandle,                                                                      // Target: void invoke(DeclaringClass, Object)
                MethodType.methodType(Void.TYPE, Any::class.java)              // Instantiated: void accept(Object t)
            )

            return site.target.invoke(instance) as Consumer<EurybiumEvent>
        } catch (e: Throwable) {
            throw InvalidRunnableException(function, e)
        }
    }

}
