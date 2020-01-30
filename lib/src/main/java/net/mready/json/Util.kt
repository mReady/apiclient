package net.mready.json

import java.lang.AssertionError

@PublishedApi
internal const val PATH_ROOT_MARKER = "[root]"

@PublishedApi
internal fun String.expandPath(key: String) = "$this > $key"

@PublishedApi
internal fun String.expandPath(index: Int) = "$this > [$index]"

internal inline fun <T> T?.jsonNullOr(block: (T) -> JsonValue): JsonValue {
    return if (this == null) {
        JsonNull()
    } else {
        block(this)
    }
}

internal inline fun <reified T> wrapValue(value: T, path: String = PATH_ROOT_MARKER): JsonValue {
    return when (value) {
        null -> JsonNull(path)
        is String -> JsonPrimitive(value, JsonPrimitive.Type.STRING, path)
        is Number -> JsonPrimitive(value.toString(), JsonPrimitive.Type.NUMBER, path)
        is Boolean -> JsonPrimitive(value.toString(), JsonPrimitive.Type.BOOLEAN, path)
        is JsonValue -> value.copyWithPath(path)
        else -> throw AssertionError()
    }
}

internal fun wrapArray(collection: Collection<Any?>?, path: String): JsonValue {
    return collection.jsonNullOr {
        JsonArray(
            it.mapIndexedTo(mutableListOf()) { index, item -> wrapValue(item, path.expandPath(index)) },
            path
        )
    }
}