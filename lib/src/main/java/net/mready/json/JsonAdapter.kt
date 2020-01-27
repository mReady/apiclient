package net.mready.json

import java.lang.RuntimeException

class JsonParseException(message: String, cause: Throwable): RuntimeException(message, cause)

interface JsonAdapter {
    val EMPTY_JSON: JsonValue

    fun parse(string: String): JsonValue
    fun buildObject(block: JsonObjectDsl.() -> Unit): JsonValue
    fun buildArray(block: JsonArrayDsl.() -> Unit): JsonValue
}