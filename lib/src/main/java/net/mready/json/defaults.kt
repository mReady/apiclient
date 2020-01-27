package net.mready.json

import net.mready.json.impl.KotlinxJsonValue

fun JsonValue.Companion.parse(string: String, adapter: JsonAdapter = KotlinxJsonValue): JsonValue {
    return adapter.parse(string)
}

fun jsonObject(adapter: JsonAdapter = KotlinxJsonValue, block: JsonObjectDsl.() -> Unit): JsonValue {
    return adapter.buildObject(block)
}

fun jsonArray(adapter: JsonAdapter = KotlinxJsonValue, block: JsonArrayDsl.() -> Unit): JsonValue {
    return adapter.buildArray(block)
}