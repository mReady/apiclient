@file:Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")

package net.mready.json

import kotlinx.serialization.Serializable
import net.mready.json.kotlinx.JsonValueSerializer

class JsonValueException(
    message: String,
    val path: String? = null,
    cause: Throwable? = null
) : RuntimeException("$message ${path?.let { " (at $path)" }.orEmpty()}", cause)

@Serializable(with = JsonValueSerializer::class)
sealed class JsonValue(internal val path: String) {
    companion object {
        fun parse(string: String, adapter: JsonAdapter = defaultJsonAdapter) = adapter.parse(string)

        operator fun invoke(): JsonValue = JsonEmpty()

        @Suppress("UNUSED_PARAMETER")
        operator fun invoke(value: Nothing?): JsonValue = JsonNull()

        operator fun invoke(value: String?): JsonValue = wrapValue(value)
        operator fun invoke(value: Number?): JsonValue = wrapValue(value)
        operator fun invoke(value: Boolean?): JsonValue = wrapValue(value)
    }

    protected inline fun throwError(e: JsonValueException): Nothing {
        throw e
    }

    internal inline fun throwInvalidType(expected: String): Nothing {
        throw JsonValueException("Element ${this::class.simpleName} is not $expected", path)
    }

    abstract fun copyWithPath(path: String): JsonValue

    open operator fun get(key: String): JsonValue {
        return JsonError(
            JsonValueException("Element ${this::class.simpleName} is not an object", path),
            path.expandPath(key)
        )
    }

    open operator fun get(index: Int): JsonValue {
        return JsonError(
            JsonValueException("Element ${this::class.simpleName} is not an array", path),
            path.expandPath(index)
        )
    }

    open operator fun set(key: String, value: JsonValue?): Unit = throwInvalidType("object")
    open operator fun set(index: Int, value: JsonValue?): Unit = throwInvalidType("array")
    open operator fun plusAssign(value: JsonValue?): Unit = throwInvalidType("array")

    open val size: Int get() = throwInvalidType("object or array")

    open val isNull: Boolean get() = false
    open val orNull: JsonValue? get() = this

    open val stringOrNull: String? get() = null
    open val string: String get() = stringOrNull ?: throwInvalidType("string")

    open val intOrNull: Int? get() = null
    open val int: Int get() = intOrNull ?: throwInvalidType("int")

    open val longOrNull: Long? get() = null
    open val long: Long get() = longOrNull ?: throwInvalidType("long")

    open val doubleOrNull: Double? get() = null
    open val double: Double get() = doubleOrNull ?: throwInvalidType("double")

    open val boolOrNull: Boolean? get() = null
    open val bool: Boolean get() = boolOrNull ?: throwInvalidType("bool")

    open val arrayOrNull: List<JsonValue>? get() = null
    open val array: List<JsonValue> get() = arrayOrNull ?: throwInvalidType("array")

    open val objOrNull: Map<String, JsonValue>? get() = null
    open val obj: Map<String, JsonValue> get() = objOrNull ?: throwInvalidType("object")

    fun toJsonString(prettyPrint: Boolean = false, adapter: JsonAdapter = defaultJsonAdapter): String {
        return adapter.stringify(this, prettyPrint)
    }

    // "extension" operators (embedded here because auto-import doesn't work great for operators)
    operator fun set(key: String, value: Nothing?) = set(key, JsonNull(path.expandPath(key)))

    operator fun set(key: String, value: String?) = set(key, wrapValue(value, path.expandPath(key)))
    operator fun set(key: String, value: Number?) = set(key, wrapValue(value, path.expandPath(key)))
    operator fun set(key: String, value: Boolean?) = set(key, wrapValue(value, path.expandPath(key)))

    @JvmName("setValues")
    operator fun set(key: String, value: Collection<JsonValue?>?) = set(key, wrapArray(value, path.expandPath(key)))

    @JvmName("setStrings")
    operator fun set(key: String, value: Collection<String?>?) = set(key, wrapArray(value, path.expandPath(key)))

    @JvmName("setNumbers")
    operator fun set(key: String, value: Collection<Number?>?) = set(key, wrapArray(value, path.expandPath(key)))

    operator fun set(index: Int, value: Nothing?) = set(index, JsonNull(path.expandPath(index)))
    operator fun set(index: Int, value: String?) = set(index, wrapValue(value, path.expandPath(index)))
    operator fun set(index: Int, value: Number?) = set(index, wrapValue(value, path.expandPath(index)))
    operator fun set(index: Int, value: Boolean?) = set(index, wrapValue(value, path.expandPath(index)))

    @JvmName("setValues")
    operator fun set(index: Int, value: Collection<JsonValue>?) = set(index, wrapArray(value, path.expandPath(index)))

    @JvmName("setStrings")
    operator fun set(index: Int, value: Collection<String?>?) = set(index, wrapArray(value, path.expandPath(index)))

    @JvmName("setNumbers")
    operator fun set(index: Int, value: Collection<Number?>?) = set(index, wrapArray(value, path.expandPath(index)))

    operator fun plusAssign(value: Nothing?) = plusAssign(JsonNull(path.expandPath(size)))
    operator fun plusAssign(value: String?) = plusAssign(wrapValue(value, path.expandPath(size)))
    operator fun plusAssign(value: Number?) = plusAssign(wrapValue(value, path.expandPath(size)))
    operator fun plusAssign(value: Boolean?) = plusAssign(wrapValue(value, path.expandPath(size)))

    @JvmName("plusValues")
    operator fun plusAssign(value: Collection<JsonValue>?) = plusAssign(wrapArray(value, path.expandPath(size)))

    @JvmName("plusStrings")
    operator fun plusAssign(value: Collection<String?>?) = plusAssign(wrapArray(value, path.expandPath(size)))

    @JvmName("plusNumbers")
    operator fun plusAssign(value: Collection<Number?>?) = plusAssign(wrapArray(value, path.expandPath(size)))
}


class JsonNull(path: String = PATH_ROOT_MARKER) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonNull(path)

    override val isNull: Boolean
        get() = true

    override val orNull: JsonValue?
        get() = null
}

class JsonObject(
    internal val content: MutableMap<String, JsonValue>,
    path: String = PATH_ROOT_MARKER
) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonObject(
        content.mapValuesTo(mutableMapOf()) { it.value.copyWithPath(path.expandPath(it.key)) },
        path
    )

    override val size: Int
        get() = content.size

    override operator fun get(key: String): JsonValue {
        return content.getOrPut(key) {
            JsonEmpty(path.expandPath(key)) { JsonValueException("No such key: $key in object", path) }
        }
    }

    override operator fun set(key: String, value: JsonValue?) {
        val childPath = path.expandPath(key)
        content[key] = jsonNullOr(childPath) {
            if (it.path == childPath) {
                it
            } else {
                it.copyWithPath(path.expandPath(key))
            }
        }
    }

    override val objOrNull: Map<String, JsonValue>?
        get() = content
}

class JsonArray(
    internal val content: MutableList<JsonValue>,
    path: String = PATH_ROOT_MARKER
) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonArray(
        content.mapIndexedTo(mutableListOf()) { index, item -> item.copyWithPath(path.expandPath(index)) },
        path
    )

    override val size: Int
        get() = content.size

    override operator fun get(index: Int): JsonValue {
        when {
            index < 0 -> throwError(JsonValueException("Invalid array index $index", path))
            index >= content.size -> {
                for (i in content.size..index) {
                    content.add(JsonEmpty("$path > [$i]") {
                        JsonValueException("Index $index out of bounds (size: ${content.size})", path)
                    })
                }
            }
        }
        return content[index]
    }

    override operator fun set(index: Int, value: JsonValue?) {
        val childPath = path.expandPath(index)
        val newValue = jsonNullOr(path) { if (it.path == childPath) it else it.copyWithPath(childPath) }

        when {
            index < 0 -> throwError(JsonValueException("Invalid array index $index", path))
            index < content.size -> content[index] = newValue
            else -> {
                for (i in content.size until index) {
                    content.add(JsonEmpty("$path > [$i]"))
                }
                content.add(newValue)
            }
        }
    }

    override operator fun plusAssign(value: JsonValue?) {
        set(size, value)
    }

    override val arrayOrNull: List<JsonValue>?
        get() = content
}

class JsonPrimitive(
    internal val content: String,
    internal val type: Type,
    path: String = PATH_ROOT_MARKER
) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonPrimitive(content, type, path)

    enum class Type {
        STRING, NUMBER, BOOLEAN, UNKNOWN
    }

    override val stringOrNull: String?
        get() = if (type == Type.STRING || type == Type.UNKNOWN) {
            content
        } else {
            null
        }

    override val intOrNull: Int?
        get() = if (type == Type.NUMBER || type == Type.UNKNOWN) {
            content.toIntOrNull()
        } else {
            null
        }


    override val longOrNull: Long?
        get() = if (type == Type.NUMBER || type == Type.UNKNOWN) {
            content.toLongOrNull()
        } else {
            null
        }

    override val doubleOrNull: Double?
        get() = if (type == Type.NUMBER || type == Type.UNKNOWN) {
            content.toDoubleOrNull()
        } else {
            null
        }

    override val boolOrNull: Boolean?
        get() = if (type == Type.BOOLEAN || type == Type.UNKNOWN) {
            content.toBoolean()
        } else {
            null
        }
}

class JsonReference(internal val value: Any, path: String = PATH_ROOT_MARKER) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonReference(value, path)
}

class JsonError(private val e: JsonValueException, path: String = PATH_ROOT_MARKER) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonError(e, path)

    override operator fun get(key: String) = this
    override operator fun get(index: Int) = this

    override val isNull: Boolean get() = true
    override val orNull: JsonValue? get() = null

    override val string: String get() = throwError(e)
    override val int: Int get() = throwError(e)
    override val long: Long get() = throwError(e)
    override val double: Double get() = throwError(e)
    override val bool: Boolean get() = throwError(e)
    override val array: List<JsonValue> get() = throwError(e)
    override val obj: Map<String, JsonValue> get() = throwError(e)
}

class JsonEmpty(
    path: String = PATH_ROOT_MARKER,
    private val pendingException: (() -> JsonValueException)? = null
) : JsonValue(path) {
    @PublishedApi
    internal var wrapped: JsonValue? = null
    private inline val defaultException get() = JsonValueException("Json element is empty", path)

    override fun copyWithPath(path: String) = wrapped?.copyWithPath(path) ?: JsonEmpty(path, pendingException)

    override val size: Int
        get() = wrapped?.size ?: 0

    private fun materializeAsObject(): JsonValue {
        if (wrapped == null) {
            wrapped = JsonObject(mutableMapOf(), path)
        } else if (wrapped !is JsonObject) {
            throwInvalidType("object")
        }

        return wrapped!!
    }

    private fun materializeAsArray(): JsonValue {
        if (wrapped == null) {
            wrapped = JsonArray(mutableListOf(), path)
        } else if (wrapped !is JsonArray) {
            throwInvalidType("array")
        }

        return wrapped!!
    }

    override operator fun get(key: String): JsonValue {
        return materializeAsObject()[key]
    }

    override operator fun set(key: String, value: JsonValue?) {
        materializeAsObject()[key] = value
    }

    override operator fun get(index: Int): JsonValue {
        return materializeAsArray()[index]
    }

    override operator fun set(index: Int, value: JsonValue?) {
        materializeAsArray()[index] = value
    }

    override operator fun plusAssign(value: JsonValue?) {
        materializeAsArray() += value
    }

    override val isNull: Boolean get() = wrapped == null
    override val orNull: JsonValue? get() = wrapped

    override val string: String get() = throwError(pendingException?.invoke() ?: defaultException)
    override val int: Int get() = throwError(pendingException?.invoke() ?: defaultException)
    override val long: Long get() = throwError(pendingException?.invoke() ?: defaultException)
    override val double: Double get() = throwError(pendingException?.invoke() ?: defaultException)
    override val bool: Boolean get() = throwError(pendingException?.invoke() ?: defaultException)

    override val arrayOrNull: List<JsonValue>? get() = wrapped?.arrayOrNull
    override val array: List<JsonValue>
        get() = wrapped?.array ?: throwError(pendingException?.invoke() ?: defaultException)

    override val objOrNull: Map<String, JsonValue>? get() = wrapped?.objOrNull
    override val obj: Map<String, JsonValue>
        get() = wrapped?.obj ?: throwError(pendingException?.invoke() ?: defaultException)
}