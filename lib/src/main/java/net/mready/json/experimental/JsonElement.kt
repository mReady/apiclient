package net.mready.json.experimental

import net.mready.json.JsonValue
import net.mready.json.JsonValueException
import kotlin.collections.set

sealed class JsonElement(internal val path: String) : JsonValue {
    companion object {
        operator fun invoke(): JsonElement {
            return JsonEmpty()
        }

        @Suppress("UNUSED_PARAMETER")
        operator fun invoke(value: Nothing?): JsonElement {
            return JsonNull()
        }

        operator fun invoke(value: String?): JsonElement {
            return wrap(value)
        }

        operator fun invoke(value: Number?): JsonElement {
            return wrap(value)
        }

        operator fun invoke(value: Boolean?): JsonElement {
            return wrap(value)
        }

        internal inline fun <reified T> wrap(value: T, path: String = PATH_ROOT_MARKER): JsonElement {
            return when (value) {
                null -> JsonNull(path)
                is String -> JsonPrimitive(value, JsonPrimitive.Type.STRING, path)
                is Number -> JsonPrimitive(value.toString(), JsonPrimitive.Type.NUMBER, path)
                is Boolean -> JsonPrimitive(value.toString(), JsonPrimitive.Type.BOOLEAN, path)
                else -> TODO()
            }
        }
    }

    protected fun throwError(e: JsonValueException): Nothing {
        throw JsonValueException(
            message = "JSON error",
            path = e.path ?: path,
            cause = e
        )
    }

    abstract fun copyWithPath(path: String): JsonElement

    override fun get(key: String): JsonElement {
        return JsonError(
            JsonValueException("Element ${this::class.simpleName} is not an object", path),
            path.expandPath(key)
        )
    }

    override fun get(index: Int): JsonElement {
        return JsonError(
            JsonValueException("Element ${this::class.simpleName} is not an array", path),
            path.expandPath(index)
        )
    }

    open operator fun set(key: String, value: JsonElement) {
        throwError(JsonValueException("Element ${this::class.simpleName} is not an object", path))
    }

    @Suppress("UNUSED_PARAMETER")
    operator fun set(key: String, value: Nothing?) {
        this[key] = JsonNull(path.expandPath(key))
    }

    operator fun set(key: String, value: String?) {
        this[key] = wrap(value, path.expandPath(key))
    }

    operator fun set(key: String, value: Number?) {
        this[key] = wrap(value, path.expandPath(key))
    }

    operator fun set(key: String, value: Boolean?) {
        this[key] = wrap(value, path.expandPath(key))
    }

    open operator fun set(index: Int, value: JsonElement) {
        throwError(JsonValueException("Element ${this::class.simpleName} is not an array", path))
    }

    @Suppress("UNUSED_PARAMETER")
    operator fun set(index: Int, value: Nothing?) {
        this[index] = JsonNull(path.expandPath(index))
    }

    operator fun set(index: Int, value: String?) {
        this[index] = wrap(value, path.expandPath(index))
    }

    operator fun set(index: Int, value: Number?) {
        this[index] = wrap(value, path.expandPath(index))
    }

    operator fun set(index: Int, value: Boolean?) {
        this[index] = wrap(value, path.expandPath(index))
    }

    open operator fun plusAssign(value: JsonElement) {
        throwError(JsonValueException("Element ${this::class.simpleName} is not an array", path))
    }

    @Suppress("UNUSED_PARAMETER")
    operator fun plusAssign(value: Nothing?) {
        this += JsonNull(path.expandPath(size))
    }

    operator fun plusAssign(value: String?) {
        this += wrap(value, path.expandPath(size))
    }

    operator fun plusAssign(value: Number?) {
        this += wrap(value, path.expandPath(size))
    }

    operator fun plusAssign(value: Boolean?) {
        this += wrap(value, path.expandPath(size))
    }

    open val size: Int
        get() = throwError(JsonValueException("Element ${this::class.simpleName} is not an object or array", path))

    override fun <T> valueOrNull(): T? {
        TODO("not implemented")
    }

    override fun <T> value(): T {
        TODO("not implemented")
    }

    override val isNull: Boolean
        get() = false

    override val orNull: JsonElement?
        get() = this

    override val stringOrNull: String?
        get() = null

    override val string: String
        get() = stringOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a string", path))

    override val intOrNull: Int?
        get() = null

    override val int: Int
        get() = intOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an int", path))

    override val longOrNull: Long?
        get() = null

    override val long: Long
        get() = longOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a long", path))

    override val doubleOrNull: Double?
        get() = null

    override val double: Double
        get() = doubleOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a double", path))

    override val boolOrNull: Boolean?
        get() = null

    override val bool: Boolean
        get() = boolOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a bool", path))

    override val arrayOrNull: List<JsonElement>?
        get() = null

    override val array: List<JsonElement>
        get() = arrayOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an array", path))

    override val objOrNull: Map<String, JsonElement>?
        get() = null

    override val obj: Map<String, JsonElement>
        get() = objOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an object", path))

    override fun toJsonString(prettyPrint: Boolean): String {
        return ExperimentalJsonAdapter.stringify(this, prettyPrint)
    }
}


class JsonNull(path: String = PATH_ROOT_MARKER) : JsonElement(path) {
    override fun copyWithPath(path: String) = JsonNull(path)

    override val isNull: Boolean
        get() = true

    override val orNull: JsonElement?
        get() = null
}

class JsonObject(
    internal val content: MutableMap<String, JsonElement>,
    path: String = PATH_ROOT_MARKER
) : JsonElement(path) {
    override fun copyWithPath(path: String) = JsonObject(content, path)

    override val size: Int
        get() = content.size

    override fun get(key: String): JsonElement {
        return content.getOrPut(key) { JsonEmpty(path.expandPath(key)) }
    }

    override fun set(key: String, value: JsonElement) {
        val childPath = path.expandPath(key)
        if (value.path == childPath) {
            content[key] = value
        } else {
            content[key] = value.copyWithPath(path.expandPath(key))
        }
    }

    override val objOrNull: Map<String, JsonElement>?
        get() = content
}

class JsonArray(
    internal val content: MutableList<JsonElement>,
    path: String = PATH_ROOT_MARKER
) : JsonElement(path) {
    override fun copyWithPath(path: String) = JsonArray(content, path)

    override val size: Int
        get() = content.size

    override fun get(index: Int): JsonElement {
        return if (index >= 0 && index < content.size) {
            content[index]
        } else {
            return JsonError(
                JsonValueException("Index $index out of bounds (size: ${content.size})", path),
                path.expandPath(index)
            )
        }
    }

    override fun set(index: Int, value: JsonElement) {
        val childPath = path.expandPath(index)
        val newValue = if (value.path == childPath) value else value.copyWithPath(childPath)

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

    override fun plusAssign(value: JsonElement) {
        set(size, value)
    }

    override val arrayOrNull: List<JsonElement>?
        get() = content
}

class JsonPrimitive(
    internal val content: String,
    internal val type: Type,
    path: String = PATH_ROOT_MARKER
) : JsonElement(path) {
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

//class JsonComplex: JsonElement()
class JsonError(private val e: JsonValueException, path: String = PATH_ROOT_MARKER) : JsonElement(path) {
    override fun copyWithPath(path: String) = JsonError(e, path)

    override fun get(key: String) = JsonError(e, path.expandPath(key))
    override fun get(index: Int) = JsonError(e, path.expandPath(index))

    override fun <T> valueOrNull(): T? = null
    override fun <T> value(): T = throwError(e)

    override val isNull: Boolean get() = true
    override val orNull: JsonElement? get() = null

    override val string: String get() = throwError(e)
    override val int: Int get() = throwError(e)
    override val long: Long get() = throwError(e)
    override val double: Double get() = throwError(e)
    override val bool: Boolean get() = throwError(e)
    override val array: List<JsonElement> get() = throwError(e)
    override val obj: Map<String, JsonElement> get() = throwError(e)
}

class JsonEmpty(
    path: String = PATH_ROOT_MARKER
) : JsonElement(path) {
    internal var wrapped: JsonElement? = null

    override fun copyWithPath(path: String) = TODO()

    override val size: Int
        get() = wrapped?.size ?: 0

    private fun materializeAsObject(): JsonElement {
        if (wrapped == null) {
            wrapped = JsonObject(mutableMapOf(), path)
        } else if (wrapped !is JsonObject) {
            throwError(JsonValueException("Element ${wrapped!!::class.simpleName} is not an object", path))
        }

        return wrapped!!
    }

    private fun materializeAsArray(): JsonElement {
        if (wrapped == null) {
            wrapped = JsonArray(mutableListOf(), path)
        } else if (wrapped !is JsonArray) {
            throwError(JsonValueException("Element ${wrapped!!::class.simpleName} is not an array", path))
        }

        return wrapped!!
    }

    override fun get(key: String): JsonElement {
        return materializeAsObject()[key]
    }

    override operator fun set(key: String, value: JsonElement) {
        materializeAsObject()[key] = value
    }

    override fun get(index: Int): JsonElement {
        return materializeAsArray()[index]
    }

    override fun set(index: Int, value: JsonElement) {
        materializeAsArray()[index] = value
    }

    override fun plusAssign(value: JsonElement) {
        materializeAsArray() += value
    }

    override val isNull: Boolean
        get() = wrapped == null

    override val orNull: JsonElement?
        get() = wrapped
}