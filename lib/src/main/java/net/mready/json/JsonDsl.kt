package net.mready.json


@DslMarker
annotation class JsonDslMarker

inline fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
    return JsonObjectDsl().apply(block).build()
}

inline fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
    return JsonArrayDsl().apply(block).build()
}

@JsonDslMarker
open class JsonDsl(@PublishedApi internal val path: String = PATH_ROOT_MARKER) {
    inline fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return JsonArrayDsl(path).apply(block).build()
    }

    inline fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return JsonObjectDsl(path).apply(block).build()
    }
}

@JsonDslMarker
class JsonObjectDsl(path: String = PATH_ROOT_MARKER) : JsonDsl(path) {
    val obj: JsonValue = JsonObject(mutableMapOf(), path)

    infix fun String.value(value: Nothing?) {
        obj[this] = value
    }

    infix fun String.value(value: String?) {
        obj[this] = value
    }

    infix fun String.value(value: Number?) {
        obj[this] = value
    }

    infix fun String.value(value: Boolean?) {
        obj[this] = value
    }

    infix fun String.value(value: JsonValue?) {
        obj[this] = value ?: JsonNull(path)
    }

    inline infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit) {
        obj[this] = JsonArrayDsl(path.expandPath(this)).apply(block).build()
    }

    inline infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit) {
        obj[this] = JsonObjectDsl(path.expandPath(this)).apply(block).build()
    }

    @PublishedApi
    internal fun build(): JsonValue {
        return obj
    }
}

@JsonDslMarker
class JsonArrayDsl(path: String = PATH_ROOT_MARKER) : JsonDsl(path) {
    val array: JsonValue = JsonArray(mutableListOf(), path)

    fun emit(value: Nothing?) {
        array += null
    }

    fun emit(value: String?) {
        array += value
    }

    fun emit(value: Number?) {
        array += value
    }

    fun emit(value: Boolean?) {
        array += value
    }

    fun emit(value: JsonValue?) {
        array += value ?: JsonNull(path.expandPath(array.size))
    }

    inline fun <T> Collection<T>.emitEach(block: (T) -> JsonValue) {
        forEach { emit(block(it)) }
    }

    @PublishedApi
    internal fun build(): JsonValue {
        return array
    }
}