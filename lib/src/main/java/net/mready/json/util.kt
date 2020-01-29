package net.mready.json

@PublishedApi
internal const val PATH_ROOT_MARKER = "[root]"

@PublishedApi
internal fun String.expandPath(key: String) = "$this > $key"

@PublishedApi
internal fun String.expandPath(index: Int) = "$this > [$index]"

internal inline fun <T> T?.jsonNullOrWrap(block: (T) -> JsonValue): JsonValue {
    return if (this == null) {
        JsonNull()
    } else {
        block(this)
    }
}