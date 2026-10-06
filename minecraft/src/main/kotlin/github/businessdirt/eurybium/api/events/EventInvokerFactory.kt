package github.businessdirt.eurybium.api.events

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandleProxies
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Consumer
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

@OptIn(ExperimentalStdlibApi::class)
object EventInvokerFactory {

    val KFunction<*>.fullyQualifiedName: String
        get() {
            val method = javaMethod
            val declaring = method?.declaringClass?.name ?: "Unknown"
            val params = valueParameters
                .joinToString(prefix = "(", postfix = ")", separator = ", ") {
                    it.type.toString().substringAfterLast('.')
                }

            return "$declaring::$name$params"
        }


    /**
     * Creates a [java.util.function.Consumer] that invokes the given method on the provided owner.
     * The method must be public, non-static, and take exactly one argument.
     *
     * @param instance the object on which the method will be invoked.
     * @param function the function to be invoked.
     * @return a [java.util.function.Consumer] that, when called, invokes the specified method.
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
            val boundHandle = privateLookup.unreflect(method).bindTo(instance)
            val consumerHandle = MethodHandles.dropArguments(boundHandle, 0, Any::class.java)

            return MethodHandleProxies.asInterfaceInstance(Consumer::class.java, consumerHandle) as Consumer<EurybiumEvent>
        } catch (e: Throwable) {
            throw InvalidRunnableException(function, e)
        }
    }

}
