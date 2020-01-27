package net.mready.json.kotlinx

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.mready.json.JsonArrayDsl
import net.mready.json.JsonObjectDsl
import net.mready.json.JsonValue


fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
    return KotlinxJsonValue(KotlinxJsonObjectDsl().apply(block).build())
}

fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
    return KotlinxJsonValue(KotlinxJsonArrayDsl().apply(block).build())
}

class KotlinxJsonObjectDsl : JsonObjectDsl {
    private val content = mutableMapOf<String, JsonElement>()

    override fun String.value(value: String?) {
        content[this] = JsonPrimitive(value)
    }

    override fun String.value(value: Number?) {
        content[this] = JsonPrimitive(value)
    }

    override fun String.value(value: Boolean?) {
        content[this] = JsonPrimitive(value)
    }

    override infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit) {
        content[this] = KotlinxJsonArrayDsl().apply(block).build()
    }

    override infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit) {
        content[this] = KotlinxJsonObjectDsl().apply(block).build()
    }

    internal fun build(): JsonObject {
        return JsonObject(content)
    }
}

class KotlinxJsonArrayDsl : JsonArrayDsl {
    private val content: MutableList<JsonElement> = mutableListOf()

    override fun emit(value: String?) {
        content += JsonPrimitive(value)
    }

    override fun emit(value: Number?) {
        content += JsonPrimitive(value)
    }

    override fun emit(value: Boolean?) {
        content += JsonPrimitive(value)
    }

    override fun emitArray(block: JsonArrayDsl.() -> Unit) {
        content.add(KotlinxJsonArrayDsl().apply(block).build())
    }

    override fun emitObject(block: JsonObjectDsl.() -> Unit) {
        content.add(KotlinxJsonObjectDsl().apply(block).build())
    }

    internal fun build(): JsonArray {
        return JsonArray(content)
    }
}