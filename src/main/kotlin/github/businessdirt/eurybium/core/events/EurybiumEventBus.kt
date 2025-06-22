package github.businessdirt.eurybium.core.events

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Consumer

object EurybiumEventBus {
    private val listeners: MutableMap<Class<out EurybiumEvent>, MutableList<EurybiumEventListener>> = mutableMapOf()
    private val handlers: MutableMap<Class<out EurybiumEvent>, EurybiumEventHandler> = mutableMapOf()

    fun init() {
        val reflections = Reflections(
            ConfigurationBuilder()
                .forPackage("github.businessdirt.eurybium")
                .setScanners(Scanners.MethodsAnnotated)
        )

        invokeAnnotatedMethods(reflections, EventCallback::class.java, ::registerEventCallbackMethod)
        invokeAnnotatedMethods(reflections, HandleEvent::class.java, ::registerHandleEventMethod)
    }

    /**
     * Invokes a function on every method with an annotation in a give package
     */
    private fun invokeAnnotatedMethods(
        reflections: Reflections,
        annotation: Class<out Annotation>,
        invoker: (Method, Any) -> Unit
    ) {
        reflections.getMethodsAnnotatedWith(annotation).forEach { method ->
            runCatching {
                method.isAccessible = true
                invoker.invoke(method, try {
                    method.declaringClass.getField("INSTANCE").get(null)
                } catch (_: NoSuchFieldException) {
                    method.declaringClass.getDeclaredConstructor().newInstance()
                })
            }.onFailure { e ->
                throw when (e) {
                    is NoSuchMethodException -> RuntimeException(
                        "Class '${method.declaringClass.simpleName}' has no default constructor " +
                                "(required for invoking @${annotation.simpleName} on method '${method.name}')", e
                    )
                    else -> RuntimeException(
                        "Failed to invoke @${annotation.simpleName} on method " +
                                "'${method.declaringClass.simpleName}.${method.name}': ${e.message}", e
                    )
                }
            }
        }
    }

    /**
     * Registers a method annotated with `EventCallback` to its minecraft callback
     * @param method the method to register
     * @param instance the object the method belongs to
     */
    private fun registerEventCallbackMethod(method: Method, instance: Any) {
        runCatching { method.invoke(instance) }
            .onFailure { e ->
                throw when (e) {
                    is IllegalAccessException -> RuntimeException(
                        "Could not access method '${method.name}' in class '${method.declaringClass.simpleName}': ${e.message}", e
                    )
                    is InvocationTargetException -> RuntimeException(
                        "Method '${method.name}' in class '${method.declaringClass.simpleName}' threw an exception: ${e.cause?.message ?: e.message}", e
                    )
                    else -> e
                }
            }
    }

    /**
     * Registers a method annotated with `HandleEvent` to an event listener
     * @param method the method to register
     * @param instance the object the method belongs to
     */
    private fun registerHandleEventMethod(method: Method, instance: Any) {
        val (options, eventTypes) = getEventData(method) ?: return
        eventTypes.forEach { eventType ->
            require(Modifier.isPublic(method.modifiers)) { "Method " + method.name + "() in " + instance.javaClass.name + " is not public. Make sure to set it to public." }
            require(method.parameterCount == 1) { "Method " + method.name + "() must have 1 parameter" }

            val name = buildListenerName(method)
            val invoker = createConsumerFromMethod(instance, method)
            listeners.getOrPut(eventType) { mutableListOf() }
                .add(EurybiumEventListener(name, invoker, options))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createConsumerFromMethod(instance: Any, method: Method): Consumer<Any> {
        try {
            val handle = MethodHandles.lookup().unreflect(method)
            return LambdaMetafactory.metafactory(
                MethodHandles.lookup(),
                "accept",
                MethodType.methodType(Consumer::class.java, instance::class.java),
                MethodType.methodType(Nothing::class.javaPrimitiveType, Object::class.java),
                handle,
                MethodType.methodType(Nothing::class.javaPrimitiveType, method.parameterTypes[0]),
            ).target.bindTo(instance).invokeExact() as Consumer<Any>
        } catch (e: Throwable) {
            throw IllegalArgumentException("Method ${instance.javaClass.name}#${method.name} is not a valid consumer", e)
        }
    }

    fun getEventHandler(event: Class<EurybiumEvent>): EurybiumEventHandler = handlers.getOrPut(event) {
        EurybiumEventHandler(event, getEventClasses(event).mapNotNull { listeners[it] }.flatten())
    }


    @Suppress("UNCHECKED_CAST")
    private fun getEventData(method: Method): Pair<HandleEvent, List<Class<out EurybiumEvent>>>? {
        val options = method.getAnnotation(HandleEvent::class.java) ?: return null

        require(method.parameterCount == 1) {
            "Method " + method.name + "() must have 1 parameter"
        }

        val eventType = method.parameterTypes.first()
        require(EurybiumEvent::class.java.isAssignableFrom(eventType)) {
            "Method " + method.name + "() must be a subclass of " + EurybiumEvent::class.java.getSimpleName()
        }
        return options to listOf(eventType as Class<out EurybiumEvent>)
    }

    private fun buildListenerName(method: Method): String {
        val paramTypesString = method.parameterTypes.joinTo(
            StringBuilder(),
            prefix = "(",
            postfix = ")",
            separator = ", ",
            transform = { it.simpleName }
        ).toString()

        return "${method.declaringClass.getName()}::${method.name}$paramTypesString"
    }

    private fun getEventClasses(clazz: Class<*>): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        classes.add(clazz)

        var currentClass = clazz
        while (currentClass.superclass != null) {
            val superClass = currentClass.superclass
            if (superClass == EurybiumEvent::class.java) break;
            if (superClass == RenderingEurybiumEvent::class.java) break;
            if (superClass == CancellableEurybiumEvent::class.java) break;
            classes.add(superClass)
            currentClass = superClass
        }
        return classes
    }
}
