package net.mready.json.kotlinx

import kotlinx.serialization.json.*
import net.mready.apiclient.JsonDeserializeException
import net.mready.apiclient.JsonParseException
import net.mready.json.JsonDeserializer
import net.mready.json.JsonValue

class JsonElementException(message: String) : RuntimeException(message)

private fun List<String>.joinPath(): String {
    return joinToString(" > ")
}

class KotlinxJsonValue(
    private val value: Any,
    private val path: List<String> = listOf("ROOT")
) : JsonValue {

    companion object: JsonDeserializer {
        override fun parse(string: String): JsonValue {
            try {
                return KotlinxJsonValue(Json.plain.parse(JsonElementSerializer, string))
            } catch (e: Throwable) {
                throw JsonParseException(e.message ?: "Unable to parse JSON", e)
            }
        }
    }

    fun asJsonElement(): JsonElement {
        if (value is JsonElement) {
            return value
        } else {
            checkOrThrow { throw AssertionError() }
        }
    }

    private fun checkOrThrow(block: () -> Exception): Nothing {
        throw JsonDeserializeException(
            message = "JSON error on ${path.joinPath()}",
            cause = (value as? Throwable) ?: block()
        )
    }

    override fun get(key: String): JsonValue {
        val childPath = path + key

        if (value is JsonElementException) {
            return KotlinxJsonValue(value, path)
        }

        if (value is JsonObject) {
            return KotlinxJsonValue(value[key] ?: JsonNull, childPath)
        } else {
            return KotlinxJsonValue(
                value = JsonElementException("Element is not an object at ${path.joinPath()}"),
                path = childPath
            )
        }
    }

    override fun get(index: Int): JsonValue {
        val childPath = path + "[$index]"

        if (value is JsonElementException) {
            return KotlinxJsonValue(value, path)
        }

        if (value is JsonArray) {
            return KotlinxJsonValue(value.getOrNull(index) ?: JsonNull, childPath)
        } else {
            return KotlinxJsonValue(
                value = JsonElementException("Element is not an array at ${path.joinPath()}"),
                path = childPath
            )
        }
    }

    override fun isNull(): Boolean {
        return value is JsonNull || value is JsonElementException
    }

    override fun orNull(): JsonValue? {
        if (value is JsonNull || value is JsonElementException) {
            return null
        } else {
            return this
        }
    }

    override fun stringOrNull(): String? {
        if (value is JsonLiteral && value.isString) {
            return value.content
        } else {
            return null
        }
    }

    override fun string(): String {
        return stringOrNull() ?: checkOrThrow {
            JsonElementException("Expected string, found $value at ${path.joinPath()}")
        }
    }

    override fun intOrNull(): Int? {
        if (value is JsonLiteral && !value.isString) {
            return value.intOrNull
        } else {
            return null
        }
    }

    override fun int(): Int {
        if (value is JsonLiteral && !value.isString) {
            return value.int
        } else {
            checkOrThrow {
                JsonElementException("Expected int, found $value at ${path.joinPath()}")
            }
        }
    }

    override fun longOrNull(): Long? {
        if (value is JsonLiteral && !value.isString) {
            return value.longOrNull
        } else {
            return null
        }
    }

    override fun long(): Long {
        if (value is JsonLiteral && !value.isString) {
            return value.long
        } else {
            checkOrThrow {
                JsonElementException("Expected long, found $value at ${path.joinPath()}")
            }
        }
    }

    override fun doubleOrNull(): Double? {
        if (value is JsonLiteral && !value.isString) {
            return value.doubleOrNull
        } else {
            return null
        }
    }

    override fun double(): Double {
        if (value is JsonLiteral && !value.isString) {
            return value.double
        } else {
            checkOrThrow {
                JsonElementException("Expected double, found $value at ${path.joinPath()}")
            }
        }
    }

    override fun boolOrNull(): Boolean? {
        if (value is JsonLiteral && !value.isString) {
            return value.booleanOrNull
        } else {
            return null
        }
    }

    override fun bool(): Boolean {
        if (value is JsonLiteral && !value.isString) {
            return value.boolean
        } else {
            checkOrThrow {
                JsonElementException("Expected boolean, found $value at ${path.joinPath()}")
            }
        }
    }

    override fun arrayOrNull(): List<JsonValue>? {
        if (value is JsonArray) {
            return value.mapIndexed { index, item -> KotlinxJsonValue(item, path + "[$index]") }
        } else {
            return null
        }
    }

    override fun array(): List<JsonValue> {
        return arrayOrNull() ?: checkOrThrow {
            JsonElementException("Expected array, found $value at ${path.joinPath()}")
        }
    }

    override fun objOrNull(): Map<String, JsonValue>? {
        if (value is JsonObject) {
            return value.mapValues { KotlinxJsonValue(it.value, path + it.key) }
        } else {
            return null
        }
    }

    override fun obj(): Map<String, JsonValue> {
        return objOrNull() ?: checkOrThrow {
            JsonElementException("Expected object, found $value at ${path.joinPath()}")
        }
    }

    override fun toJsonString(): String {
        if (value is JsonElement) {
            return Json.plain.stringify(JsonElementSerializer, value)
        } else {
            checkOrThrow { throw AssertionError() }
        }
    }
}