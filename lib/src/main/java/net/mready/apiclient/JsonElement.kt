@file:Suppress("unused")

package net.mready.apiclient

sealed class JsonException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class JsonParseException(message: String, cause: Throwable? = null) : JsonException(message, cause)
class JsonElementException(message: String, cause: Throwable? = null) :
    JsonException(message, cause)

class JsonNullElementException(message: String) : JsonException(message)
class JsonEmptyElementException(message: String) : JsonException(message)

interface JsonSerializer {
    fun string(value: Any?): String

    fun parse(json: String): JsonElement
}

class JsonElement(
    @PublishedApi internal val rawValue: Any,
    @PublishedApi internal val path: List<String> = listOf("[root]")
) {

    operator fun get(key: String): JsonElement {
        val newPath = path + key

        if (rawValue is JsonElementException) return JsonElement(rawValue, newPath)

        if (rawValue is Map<*, *>) {
            return JsonElement(
                rawValue = rawValue[key]
                    ?: JsonNullElementException("Null element for key $key as ${newPath.joinPath()}"),
                path = newPath
            )
        } else {
            return JsonElement(
                rawValue = JsonElementException("Element is not an object at ${path.joinPath()}"),
                path = newPath
            )
        }
    }

    operator fun get(index: Int): JsonElement {
        val newPath = path + "[$index]"

        if (rawValue is JsonElementException) return JsonElement(rawValue, newPath)

        if (rawValue is List<*>) {
            if (index < rawValue.size) {
                return JsonElement(
                    rawValue = rawValue[index]
                        ?: JsonNullElementException("Null element for index $index at ${newPath.joinPath()}"),
                    path = newPath
                )
            } else {
                return JsonElement(
                    rawValue = JsonElementException("Index $index (size: ${rawValue.size}) out of bounds at ${newPath.joinPath()}"),
                    path = newPath
                )
            }
        } else {
            return JsonElement(
                rawValue = JsonElementException("Element is not an array at ${path.joinPath()}"),
                path = newPath
            )
        }
    }

    fun isNull(): Boolean = rawValue is JsonNullElementException

    internal inline fun <reified T> valueOrNull(): T? {
        return rawValue as? T
    }

    internal inline fun <reified T> value(): T {
        checkError()

        if (rawValue is T) {
            return rawValue
        } else {
            throw JsonElementException("Expected ${T::class.simpleName} found ${rawValue::class.simpleName} at ${path.joinPath()}")
        }
    }

    fun <T> asList(mapper: (JsonElement) -> T): List<T> {
        checkError()

        if (rawValue is List<*>) {
            return rawValue.indices.map { mapper(get(it)) }
        } else {
            throw JsonElementException("Expected array found ${rawValue::class.simpleName} at ${path.joinPath()}")
        }
    }

    fun <T> asListOrNull(mapper: (JsonElement) -> T): List<T>? {
        checkError()

        if (rawValue is List<*>) {
            return rawValue.indices.map { mapper(get(it)) }
        } else {
            return null
        }
    }

    fun <T> asMap(mapper: (JsonElement) -> T): Map<String, T> {
        checkError()

        if (rawValue is Map<*, *>) {
            return rawValue.keys.map { it.toString() to mapper(get(it.toString())) }.toMap()
        } else {
            throw JsonElementException("Expected array found ${rawValue::class.simpleName} at ${path.joinPath()}")
        }
    }

    fun <T> asMapOrNull(mapper: (JsonElement) -> T): Map<String, T>? {
        checkError()

        if (rawValue is Map<*, *>) {
            return rawValue.keys.map { it.toString() to mapper(get(it.toString())) }.toMap()
        } else {
            return null
        }
    }

    internal fun checkError() {
        if (rawValue is JsonException) throw JsonElementException(
            "JSON error on ${path.joinPath()}",
            rawValue
        )
    }

    internal fun List<String>.joinPath(): String {
        return joinToString(" > ")
    }
}

inline fun JsonElement.stringOrElse(default: () -> String) = stringOrNull() ?: default()
fun JsonElement.stringOrNull(): String? = valueOrNull()
fun JsonElement.string(): String = value()

inline fun JsonElement.intOrElse(default: () -> Int) = intOrNull() ?: default()
fun JsonElement.intOrNull(): Int? = valueOrNull<Number>()?.toInt()
fun JsonElement.int(): Int = value<Number>().toInt()

inline fun JsonElement.longOrElse(default: () -> Long) = longOrNull() ?: default()
fun JsonElement.longOrNull(): Long? = valueOrNull<Number>()?.toLong()
fun JsonElement.long(): Long = value<Number>().toLong()

inline fun JsonElement.doubleOrElse(default: () -> Double) = doubleOrNull() ?: default()
fun JsonElement.doubleOrNull(): Double? = valueOrNull<Number>()?.toDouble()
fun JsonElement.double(): Double = value<Number>().toDouble()

inline fun JsonElement.boolOrElse(default: () -> Boolean) = boolOrNull() ?: default()
fun JsonElement.boolOrNull(): Boolean? = valueOrNull()
fun JsonElement.bool(): Boolean = value()

inline fun JsonElement.asListOrElse(default: () -> List<JsonElement>) = asListOrNull() ?: default()
fun JsonElement.asListOrNull(): List<JsonElement>? = valueOrNull()
fun JsonElement.asList(): List<JsonElement> = value()

inline fun JsonElement.asMapOrElse(default: () -> Map<String, JsonElement>) = asMapOrNull() ?: default()
fun JsonElement.asMapOrNull(): Map<String, JsonElement>? = valueOrNull()
fun JsonElement.asMap(): Map<String, JsonElement> = value()