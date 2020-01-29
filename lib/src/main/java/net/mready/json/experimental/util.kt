package net.mready.json.experimental

internal const val PATH_ROOT_MARKER = "[root]"
internal fun String.expandPath(key: String) = "$this > $key"
internal fun String.expandPath(index: Int) = "$this > [$index]"

internal inline fun <T> T?.jsonNullOrWrap(block: (T) -> JsonElement): JsonElement {
    return if (this == null) {
        JsonNull()
    } else {
        block(this)
    }
}