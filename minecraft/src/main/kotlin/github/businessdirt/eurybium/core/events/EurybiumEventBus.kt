package github.businessdirt.eurybium.core.events

import github.businessdirt.eurybium.EurybiumMod
import github.businessdirt.eurybium.core.utils.ReflectionUtils
import github.businessdirt.eurybium.core.utils.ReflectionUtils.fullyQualifiedName
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

object EurybiumEventBus {
    private val listeners: MutableMap<KClass<out EurybiumEvent>, MutableList<EurybiumEventListener>> = mutableMapOf()
    private val handlers: MutableMap<KClass<out EurybiumEvent>, EurybiumEventHandler> = mutableMapOf()

    fun init(instances: List<Any>) = instances.forEach(::register)

    @Synchronized
    fun register(instance: Any) = instance::class.declaredFunctions.forEach { registerFunction(instance, it) }

    private fun registerFunction(instance: Any, function: KFunction<*>) {
        val (options, eventTypes) = getEventData(function) ?: return

        function.isAccessible = true
        eventTypes.forEach { eventType ->
            val invoker: Consumer<EurybiumEvent> = when (function.valueParameters.size) {
                0 -> ReflectionUtils.createZeroParameterEventConsumer(instance, function)
                1 -> ReflectionUtils.createSingleParameterEventConsumer(instance, function)
                else -> throw IllegalArgumentException("Unsupported parameter count ${function.valueParameters.size}")
            }

            listeners.getOrPut(eventType) { mutableListOf() }.add(EurybiumEventListener(function.fullyQualifiedName, invoker, options))
            // Cached handlers also include superclass listeners; refresh every affected hierarchy.
            handlers.clear()

            EurybiumMod.logger.atDebug().log("Registering event listener ${function.fullyQualifiedName}")
        }
    }

    @Synchronized
    fun getEventHandler(event: KClass<out EurybiumEvent>): EurybiumEventHandler = handlers.getOrPut(event) {
        EurybiumEventHandler(event, event.eventClassHierarchy().mapNotNull { listeners[it] }.flatten())
    }


    @Suppress("UNCHECKED_CAST")
    private fun getEventData(function: KFunction<*>): Pair<HandleEvent, List<KClass<out EurybiumEvent>>>? {
        val options = function.findAnnotation<HandleEvent>() ?: return null

        return when (function.valueParameters.size) {
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
        val paramType: KType = function.valueParameters[0].type
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
