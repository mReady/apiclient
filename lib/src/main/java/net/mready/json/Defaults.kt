package net.mready.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElementSerializer
import net.mready.json.impl.JsonArrayDslImpl
import net.mready.json.impl.JsonObjectDslImpl
import net.mready.json.impl.JsonValueImpl


object DefaultJsonAdapter : JsonAdapter {
    override val EMPTY_JSON: JsonValue by lazy { JsonValueImpl(JsonValueException("Empty JSON")) }

    override fun parse(string: String): JsonValue {
        if (string.isBlank()) {
            return EMPTY_JSON
        }

        try {
            @Suppress("EXPERIMENTAL_API_USAGE")
            return JsonValueImpl(Json.parse(JsonElementSerializer, string))
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun buildObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return JsonValueImpl(JsonObjectDslImpl().apply(block).build())
    }

    override fun buildArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return JsonValueImpl(JsonArrayDslImpl().apply(block).build())
    }
}

fun JsonValue.Companion.parse(string: String, adapter: JsonAdapter = DefaultJsonAdapter): JsonValue {
    return adapter.parse(string)
}

fun jsonObject(adapter: JsonAdapter = DefaultJsonAdapter, block: JsonObjectDsl.() -> Unit): JsonValue {
    return adapter.buildObject(block)
}

fun jsonArray(adapter: JsonAdapter = DefaultJsonAdapter, block: JsonArrayDsl.() -> Unit): JsonValue {
    return adapter.buildArray(block)
}