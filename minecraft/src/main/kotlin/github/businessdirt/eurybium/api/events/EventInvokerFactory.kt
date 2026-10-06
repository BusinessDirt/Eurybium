package github.businessdirt.eurybium.api.events

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandleProxies
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Consumer
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

/** Builds event callbacks once during registration so dispatch does not repeatedly use Kotlin reflection. */
object EventInvokerFactory {

    /** A declaring-class, method-name, and parameter description suitable for registration diagnostics. */
    val KFunction<*>.fullyQualifiedName: String
        get() {
            val declaring = javaMethod?.declaringClass?.name ?: "Unknown"
            val parameters = valueParameters.joinToString(prefix = "(", postfix = ")") {
                it.type.toString().substringAfterLast('.')
            }

            return "$declaring::$name$parameters"
        }

    /**
     * Creates a callback for a Unit-returning instance method with one event parameter.
     * Private methods are supported through a lookup scoped to their declaring class.
     *
     * @throws InvalidConsumerException if ownership, signature, or method-handle creation is invalid.
     */
    @Throws(InvalidConsumerException::class)
    fun createSingleParameterEventConsumer(instance: Any, function: KFunction<*>): Consumer<EurybiumEvent> {
        try {
            val method = listenerMethod(instance, function, parameterCount = 1)
            val eventClass = method.parameterTypes.single()
            require(EurybiumEvent::class.java.isAssignableFrom(eventClass)) { "Parameter must be an event" }

            val lookup = MethodHandles.privateLookupIn(method.declaringClass, MethodHandles.lookup())
            val handle = lookup.unreflect(method)

            // The erased Consumer accepts Any; its generated implementation casts to the concrete
            // event type and invokes the method directly on the captured listener instance.
            val factory = LambdaMetafactory.metafactory(
                lookup,
                "accept",
                MethodType.methodType(Consumer::class.java, method.declaringClass),
                MethodType.methodType(Void.TYPE, Any::class.java),
                handle,
                MethodType.methodType(Void.TYPE, eventClass),
            )

            @Suppress("UNCHECKED_CAST")
            return factory.target.invoke(instance) as Consumer<EurybiumEvent>
        } catch (failure: Throwable) {
            if (failure is Error) throw failure
            throw InvalidConsumerException(function, failure)
        }
    }

    /**
     * Creates a callback for a Unit-returning instance method without an event parameter.
     * Private methods are supported; the incoming event is deliberately ignored.
     *
     * @throws InvalidRunnableException if ownership, signature, or method-handle creation is invalid.
     */
    @Throws(InvalidRunnableException::class)
    fun createZeroParameterEventConsumer(instance: Any, function: KFunction<*>): Consumer<EurybiumEvent> {
        try {
            val method = listenerMethod(instance, function, parameterCount = 0)
            val lookup = MethodHandles.privateLookupIn(method.declaringClass, MethodHandles.lookup())
            val bound = lookup.unreflect(method).bindTo(instance)

            // Bind the owner first, then add an ignored argument to adapt () -> Unit
            // to Consumer.accept(Any) without invoking the reflective KFunction on every event.
            val consumer = MethodHandles.dropArguments(bound, 0, Any::class.java)

            @Suppress("UNCHECKED_CAST")
            return MethodHandleProxies.asInterfaceInstance(Consumer::class.java, consumer) as Consumer<EurybiumEvent>
        } catch (failure: Throwable) {
            if (failure is Error) throw failure
            throw InvalidRunnableException(function, failure)
        }
    }

    private fun listenerMethod(instance: Any, function: KFunction<*>, parameterCount: Int): Method {
        val method = requireNotNull(function.javaMethod) { "Listener has no Java method" }
        require(!Modifier.isStatic(method.modifiers)) { "Listener must be an instance method" }
        require(method.declaringClass.isInstance(instance)) { "Listener owner does not match its declaring class" }
        require(method.returnType == Void.TYPE) { "Event listeners must return Unit" }
        require(method.parameterCount == parameterCount) { "Expected $parameterCount event parameters" }

        return method
    }
}
