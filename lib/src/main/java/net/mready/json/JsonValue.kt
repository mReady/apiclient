package net.mready.json

class JsonValueException(message: String, val path: String? = null, cause: Throwable? = null)
    : RuntimeException("$message ${path?.let { " (at $path)" }.orEmpty()}", cause)

interface JsonValue {
    // Allow extensions on companion object
    companion object {}

    operator fun get(key: String): JsonValue
    operator fun get(index: Int): JsonValue

    fun <T> valueOrNull(): T?
    fun <T> value(): T

    val isNull: Boolean
    val orNull: JsonValue?

    val stringOrNull: String?
    val string: String

    val intOrNull: Int?
    val int: Int

    val longOrNull: Long?
    val long: Long

    val doubleOrNull: Double?
    val double: Double

    val boolOrNull: Boolean?
    val bool: Boolean

    val arrayOrNull: List<JsonValue>?
    val array: List<JsonValue>

    val objOrNull: Map<String, JsonValue>?
    val obj: Map<String, JsonValue>

    fun toJsonString(prettyPrint: Boolean = false): String
}