package net.mready.json.impl

import kotlinx.serialization.json.*
import net.mready.json.*


class KotlinxJsonValue internal constructor(
    private val value: Any, // JsonValueException | kotlinx.serialization.json.JsonElement
    private val path: List<String> = listOf("[root]")
) : JsonValue {

    companion object : JsonAdapter {
        override val EMPTY_JSON: JsonValue by lazy { KotlinxJsonValue(JsonValueException("Empty JSON")) }

        override fun parse(string: String): JsonValue {
            if (string.isBlank()) {
                return EMPTY_JSON
            }

            try {
                @Suppress("EXPERIMENTAL_API_USAGE")
                return KotlinxJsonValue(Json.parse(JsonElementSerializer, string))
            } catch (e: Throwable) {
                throw JsonParseException(e.message ?: "Unable to parse JSON", e)
            }
        }

        override fun buildObject(block: JsonObjectDsl.() -> Unit): JsonValue {
            return KotlinxJsonValue(KotlinxJsonObjectDsl().apply(block).build())
        }

        override fun buildArray(block: JsonArrayDsl.() -> Unit): JsonValue {
            return KotlinxJsonValue(KotlinxJsonArrayDsl().apply(block).build())
        }
    }

    fun asJsonElement(): JsonElement {
        if (value is JsonElement) {
            return value
        } else {
            checkErrorOrThrow { throw AssertionError() }
        }
    }

    private val pathString: String get() = path.joinToString(" > ")

    private fun checkErrorOrThrow(block: () -> Exception): Nothing {
        throw JsonValueException(
            message = "JSON error on $pathString",
            cause = (value as? Throwable) ?: block()
        )
    }

    override fun get(key: String): JsonValue {
        val childPath = path + key

        return when (value) {
            is JsonValueException -> KotlinxJsonValue(value, childPath)
            is JsonObject -> KotlinxJsonValue(value[key] ?: JsonNull, childPath)
            else -> KotlinxJsonValue(
                value = JsonValueException("Element is not an object at $pathString"),
                path = childPath
            )
        }
    }

    override fun get(index: Int): JsonValue {
        val childPath = path + "[$index]"

        return when (value) {
            is JsonValueException -> KotlinxJsonValue(value, childPath)
            is JsonArray -> KotlinxJsonValue(value.getOrNull(index) ?: JsonNull, childPath)
            else -> KotlinxJsonValue(
                value = JsonValueException("Element is not an array at $pathString"),
                path = childPath
            )
        }
    }

    override val isNull: Boolean
        get() = value is JsonNull || value is JsonValueException

    override val orNull: JsonValue?
        get() = if (value is JsonNull || value is JsonValueException) {
            null
        } else {
            this
        }

    override val stringOrNull: String?
        get() = if (value is JsonLiteral && value.isString) {
            value.content
        } else {
            null
        }

    override val string: String
        get() = stringOrNull ?: checkErrorOrThrow {
            JsonValueException("Expected string, found $value at $pathString")
        }

    override val intOrNull: Int?
        get() = if (value is JsonLiteral && !value.isString) {
            value.intOrNull
        } else {
            null
        }

    override val int: Int
        get() = if (value is JsonLiteral && !value.isString) {
            value.int
        } else {
            checkErrorOrThrow {
                JsonValueException("Expected int, found $value at $pathString")
            }
        }

    override val longOrNull: Long?
        get() = if (value is JsonLiteral && !value.isString) {
            value.longOrNull
        } else {
            null
        }

    override val long: Long
        get() = if (value is JsonLiteral && !value.isString) {
            value.long
        } else {
            checkErrorOrThrow {
                JsonValueException("Expected long, found $value at $pathString")
            }
        }

    override val doubleOrNull: Double?
        get() = if (value is JsonLiteral && !value.isString) {
            value.doubleOrNull
        } else {
            null
        }

    override val double: Double
        get() = if (value is JsonLiteral && !value.isString) {
            value.double
        } else {
            checkErrorOrThrow {
                JsonValueException("Expected double, found $value at $pathString")
            }
        }

    override val boolOrNull: Boolean?
        get() = if (value is JsonLiteral && !value.isString) {
            value.booleanOrNull
        } else {
            null
        }

    override val bool: Boolean
        get() = if (value is JsonLiteral && !value.isString) {
            value.boolean
        } else {
            checkErrorOrThrow {
                JsonValueException("Expected boolean, found $value at $pathString")
            }
        }

    override val arrayOrNull: List<JsonValue>?
        get() = if (value is JsonArray) {
            value.mapIndexed { index, item -> KotlinxJsonValue(item, path + "[$index]") }
        } else {
            null
        }

    override val array: List<JsonValue>
        get() = arrayOrNull ?: checkErrorOrThrow {
            JsonValueException("Expected array, found $value at $pathString")
        }

    override val objOrNull: Map<String, JsonValue>?
        get() = if (value is JsonObject) {
            value.mapValues { KotlinxJsonValue(it.value, path + it.key) }
        } else {
            null
        }

    override val obj: Map<String, JsonValue>
        get() = objOrNull ?: checkErrorOrThrow {
            JsonValueException("Expected object, found $value at $pathString")
        }

    override fun toJsonString(prettyPrint: Boolean): String {
        if (value is JsonElement) {
            @Suppress("EXPERIMENTAL_API_USAGE")
            val config = JsonConfiguration(
                prettyPrint = prettyPrint,
                //TODO: What is this for?
                useArrayPolymorphism = true
            )
            return Json(config).stringify(JsonElementSerializer, value)
        } else {
            checkErrorOrThrow { throw AssertionError() }
        }
    }
}