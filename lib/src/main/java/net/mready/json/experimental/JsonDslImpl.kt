package net.mready.json.experimental

import net.mready.json.JsonArrayDsl
import net.mready.json.JsonDsl
import net.mready.json.JsonObjectDsl
import net.mready.json.JsonValue

internal open class ExJsonDsl : JsonDsl {
    override fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return ExJsonArrayDsl().apply(block).build()
    }

    override fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return ExJsonObjectDsl().apply(block).build()
    }
}

internal class ExJsonObjectDsl : ExJsonDsl(), JsonObjectDsl {
    private val content = mutableMapOf<String, JsonElement>()

    override fun String.value(value: Nothing?) {
        content[this] = JsonNull()
    }

    override fun String.value(value: String?) {
        content[this] = value?.let { JsonPrimitive(it, JsonPrimitive.Type.STRING) } ?: JsonNull()
    }

    override fun String.value(value: Number?) {
        content[this] = value?.let { JsonPrimitive(it.toString(), JsonPrimitive.Type.NUMBER) } ?: JsonNull()
    }

    override fun String.value(value: Boolean?) {
        content[this] = value?.let { JsonPrimitive(it.toString(), JsonPrimitive.Type.BOOLEAN) } ?: JsonNull()
    }

    override fun String.value(value: JsonValue?) {
        content[this] = when (value) {
            null -> JsonNull()
            is JsonElement -> value
            else -> throw IllegalArgumentException("JsonValue must be built with the same adapter")
        }
    }

    override infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit) {
        content[this] = ExJsonArrayDsl().apply(block).build()
    }

    override infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit) {
        content[this] = ExJsonObjectDsl().apply(block).build()
    }

    internal fun build(): JsonElement {
        return JsonObject(content)
    }
}

internal class ExJsonArrayDsl : ExJsonDsl(), JsonArrayDsl {
    private val content: MutableList<JsonElement> = mutableListOf()

    override val array = JsonArrayDsl.ArrayItemsCollector

    override fun emit(value: Nothing?) {
        content += JsonNull()
    }

    override fun emit(value: String?) {
        content += value?.let { JsonPrimitive(it, JsonPrimitive.Type.STRING) } ?: JsonNull()
    }

    override fun emit(value: Number?) {
        content += value?.let { JsonPrimitive(it.toString(), JsonPrimitive.Type.NUMBER) } ?: JsonNull()
    }

    override fun emit(value: Boolean?) {
        content += value?.let { JsonPrimitive(it.toString(), JsonPrimitive.Type.BOOLEAN) } ?: JsonNull()
    }

    override fun emit(value: JsonValue?) {
        content += when (value) {
            null -> JsonNull()
            is JsonElement -> value
            else -> throw IllegalArgumentException("JsonValue must be built with the same adapter")
        }
    }

    internal fun build(): JsonElement {
        return JsonArray(content)
    }
}