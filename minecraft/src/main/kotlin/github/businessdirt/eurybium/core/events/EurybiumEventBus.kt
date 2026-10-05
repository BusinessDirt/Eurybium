package github.businessdirt.eurybium.core.events

import github.businessdirt.eurybium.core.utils.ReflectionUtils
import github.businessdirt.eurybium.core.utils.ReflectionUtils.fullyQualifiedName
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible

object EurybiumEventBus {
    private val listeners: MutableMap<KClass<out EurybiumEvent>, MutableList<EurybiumEventListener>> = mutableMapOf()
    private val handlers: MutableMap<KClass<out EurybiumEvent>, EurybiumEventHandler> = mutableMapOf()

    fun init(instances: List<Any>) = instances.forEach(::register)

    @Synchronized
    fun register(instance: Any) {
        instance::class.declaredFunctions.forEach { function ->
            function.isAccessible = true
            if (function.visibility != KVisibility.PUBLIC) throw MethodNotPublicException(function)

            val (options, eventTypes) = getEventData(function)
            eventTypes.forEach { eventType ->
                val invoker: Consumer<EurybiumEvent> = when (function.parameters.size) {
                    0 -> ReflectionUtils.createZeroParameterEventConsumer(instance, function)
                    1 -> ReflectionUtils.createSingleParameterEventConsumer(instance, function)
                    else -> throw IllegalArgumentException("Unsupported parameter count ${function.parameters.size}")
                }

                listeners.getOrPut(eventType) { mutableListOf() }.add(EurybiumEventListener(function.fullyQualifiedName, invoker, options))
            }
        }
    }

    @Synchronized
    fun getEventHandler(event: KClass<out EurybiumEvent>): EurybiumEventHandler = handlers.getOrPut(event) {
        EurybiumEventHandler(event, event.eventClassHierarchy().mapNotNull { listeners[it] }.flatten())
    }


    @Suppress("UNCHECKED_CAST")
    private fun getEventData(function: KFunction<*>): Pair<HandleEvent, List<KClass<out EurybiumEvent>>> {
        val options = requireNotNull(function.findAnnotation<HandleEvent>())

        return when (function.parameters.size) {
            0 -> handleZeroParameterFunction(options)
            1 -> handleSingleParameterFunction(function, options)
            else -> throw ParameterException(function, "must have either 0 or 1 parameters")
        }
    }

    private fun handleZeroParameterFunction(options: HandleEvent): Pair<HandleEvent, List<KClass<out EurybiumEvent>>> =
        when (options.eventTypes.size) {
            0 -> options to listOf(options.eventType)
            else -> options to options.eventTypes.toList()
        }

    private fun handleSingleParameterFunction(function: KFunction<*>, options: HandleEvent): Pair<HandleEvent, List<KClass<out EurybiumEvent>>> {
        val paramType: KType = function.parameters[0].type
        val eventClass = paramType.classifier as? KClass<*>
            ?: throw ParameterException(function, "parameter must be a class")

        if (!EurybiumEvent::class.isSuperclassOf(eventClass)) throw ParameterException(
            function, "must be a subtype of " + EurybiumEvent::class.java.name
        )

        @Suppress("UNCHECKED_CAST")
        return options to listOf(eventClass as KClass<out EurybiumEvent>)
    }

    /**
     * Returns the class hierarchy for this event class up to, but excluding,
     * the base framework classes ([EurybiumEvent] and [CancellableEurybiumEvent]).
     */
    private fun KClass<*>.eventClassHierarchy(): List<KClass<*>> {
        val hierarchy = mutableListOf<KClass<*>>()
        var current: KClass<*>? = this

        while (current != null && current != Any::class) {
            if (current == EurybiumEvent::class
                || current == CancellableEurybiumEvent::class) break

            hierarchy.add(current)

            // Find the next concrete superclass, ignoring interfaces and Any
            current = current.superclasses.firstOrNull { it != Any::class && !it.java.isInterface }
        }

        return hierarchy
    }
}
