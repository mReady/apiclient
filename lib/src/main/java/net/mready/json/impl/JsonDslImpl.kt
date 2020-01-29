package net.mready.json.impl

import kotlinx.serialization.json.*
import net.mready.json.JsonArrayDsl
import net.mready.json.JsonDsl
import net.mready.json.JsonObjectDsl
import net.mready.json.JsonValue

internal open class JsonDslImpl: JsonDsl {
    override fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return JsonValueImpl(JsonArrayDslImpl().apply(block).build())
    }

    override fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return JsonValueImpl(JsonObjectDslImpl().apply(block).build())
    }
}

internal class JsonObjectDslImpl : JsonDslImpl(), JsonObjectDsl {
    private val content = mutableMapOf<String, JsonElement>()

    override fun String.value(value: Nothing?) {
        content[this] = JsonNull
    }

    override fun String.value(value: String?) {
        content[this] = JsonPrimitive(value)
    }

    override fun String.value(value: Number?) {
        content[this] = JsonPrimitive(value)
    }

    override fun String.value(value: Boolean?) {
        content[this] = JsonPrimitive(value)
    }

    override fun String.value(value: JsonValue?) {
        content[this] = when(value) {
            null -> JsonNull
            is JsonValueImpl -> value.value()
            else -> throw IllegalArgumentException("JsonValue must be built with the same adapter")
        }
    }

    override infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit) {
        content[this] = JsonArrayDslImpl().apply(block).build()
    }

    override infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit) {
        content[this] = JsonObjectDslImpl().apply(block).build()
    }

    internal fun build(): JsonObject {
        return JsonObject(content)
    }
}

internal class JsonArrayDslImpl : JsonDslImpl(), JsonArrayDsl {
    private val content: MutableList<JsonElement> = mutableListOf()

    override val array = JsonArrayDsl.ArrayItemsCollector

    override fun emit(value: Nothing?) {
        content += JsonNull
    }

    override fun emit(value: String?) {
        content += JsonPrimitive(value)
    }

    override fun emit(value: Number?) {
        content += JsonPrimitive(value)
    }

    override fun emit(value: Boolean?) {
        content += JsonPrimitive(value)
    }

    override fun emit(value: JsonValue?) {
        content += when(value) {
            null -> JsonNull
            is JsonValueImpl -> value.value<JsonElement>()
            else -> throw IllegalArgumentException("JsonValue must be built with the same adapter")
        }
    }

    internal fun build(): JsonArray {
        return JsonArray(content)
    }
}