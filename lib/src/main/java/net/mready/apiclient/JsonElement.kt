@file:Suppress("unused")

package net.mready.apiclient

sealed class JsonException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class JsonParseException(message: String, cause: Throwable? = null) : JsonException(message, cause)
class JsonDeserializeException(message: String, cause: Throwable? = null) : JsonException(message, cause)

sealed class JsonElementException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class JsonInvalidElement(message: String) : JsonElementException(message)
class JsonNullElement(message: String) : JsonElementException(message)

interface JsonSerializer {
    fun string(value: Any?): String
    fun parse(json: String): JsonElement
}

class JsonElement(
    private val rawValue: Any, // String | Number | Boolean | Map | List | JsonElementException
    private val path: List<String> = listOf("[root]")
) {

    operator fun get(key: String): JsonElement {
        val newPath = path + key

        if (rawValue is JsonElementException) return JsonElement(rawValue, newPath)

        if (rawValue is Map<*, *>) {
            return JsonElement(
                rawValue = rawValue[key]
                    ?: JsonNullElement("Null element for key $key as ${newPath.joinPath()}"),
                path = newPath
            )
        } else {
            return JsonElement(
                rawValue = JsonInvalidElement("Element is not an object at ${path.joinPath()}"),
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
                        ?: JsonNullElement("Null element for index $index at ${newPath.joinPath()}"),
                    path = newPath
                )
            } else {
                return JsonElement(
                    rawValue = JsonInvalidElement("Index $index (size: ${rawValue.size}) out of bounds at ${newPath.joinPath()}"),
                    path = newPath
                )
            }
        } else {
            return JsonElement(
                rawValue = JsonInvalidElement("Element is not an array at ${path.joinPath()}"),
                path = newPath
            )
        }
    }

    fun isNull(): Boolean = rawValue is JsonNullElement

    internal inline fun <reified T> valueOrNull(): T? {
        return rawValue as? T
    }

    internal inline fun <reified T> value(): T {
        checkError()

        if (rawValue is T) {
            return rawValue
        } else {
            throwError(JsonInvalidElement("Expected ${T::class.simpleName} found ${rawValue::class.simpleName} at ${path.joinPath()}"))
        }
    }

    fun getOrNull(): JsonElement? {
        if (rawValue is JsonElementException) {
            return null
        } else {
            return this
        }
    }

    fun <T> transformOrNull(block: (JsonElement) -> T): T? {
        return getOrNull()?.let(block)
    }

    fun <T> asList(mapper: (JsonElement) -> T): List<T> {
        if (rawValue is List<*>) {
            return rawValue.indices.map { mapper(get(it)) }
        } else {
            checkError()
            throwError(JsonInvalidElement("Expected array found ${rawValue::class.simpleName} at ${path.joinPath()}"))
        }
    }

    fun <T> asListOrNull(mapper: (JsonElement) -> T): List<T>? {
        if (rawValue is List<*>) {
            return rawValue.indices.map { mapper(get(it)) }
        } else {
            return null
        }
    }

    fun <T> asMap(mapper: (JsonElement) -> T): Map<String, T> {
        if (rawValue is Map<*, *>) {
            return rawValue.keys.map { it.toString() to mapper(get(it.toString())) }.toMap()
        } else {
            checkError()
            throwError(JsonInvalidElement("Expected object found ${rawValue::class.simpleName} at ${path.joinPath()}"))
        }
    }

    fun <T> asMapOrNull(mapper: (JsonElement) -> T): Map<String, T>? {
        if (rawValue is Map<*, *>) {
            return rawValue.keys.map { it.toString() to mapper(get(it.toString())) }.toMap()
        } else {
            return null
        }
    }

    private fun checkError() {
        if (rawValue is JsonElementException) {
            throwError(rawValue)
        }
    }

    private fun throwError(e: Exception): Nothing {
        throw JsonDeserializeException("JSON error on ${path.joinPath()}", e)
    }

    private fun List<String>.joinPath(): String {
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