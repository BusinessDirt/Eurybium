package github.businessdirt.eurybium.config.manager

import org.slf4j.Logger
import java.util.EnumMap
import kotlin.reflect.KMutableProperty0

class ConfigHolder() : EnumMap<ConfigFileType, Any>(ConfigFileType::class.java) {

    fun checkLoaded(logger: Logger) {
        if (this.isNotEmpty())
            logger.warn("Loading config despite config being already loaded")
    }

    fun set(type: ConfigFileType, value: Any) {
        require(value.javaClass == type.clazz)
        @Suppress("UNCHECKED_CAST")
        (type.property as KMutableProperty0<Any>).set(value)
        (this as MutableMap<ConfigFileType, Any>)[type] = value
    }
}