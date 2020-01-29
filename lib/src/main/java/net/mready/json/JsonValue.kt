package net.mready.json

class JsonValueException(
    message: String,
    val path: String? = null,
    cause: Throwable? = null
) : RuntimeException("$message ${path?.let { " (at $path)" }.orEmpty()}", cause)

sealed class JsonValue(internal val path: String) {
    companion object {
        fun parse(string: String, adapter: JsonAdapter = DefaultJsonAdapter): JsonValue {
            return adapter.parse(string)
        }

        operator fun invoke(): JsonValue = JsonEmpty()

        @Suppress("UNUSED_PARAMETER")
        operator fun invoke(value: Nothing?): JsonValue = JsonNull()
        operator fun invoke(value: String?): JsonValue = wrap(value)
        operator fun invoke(value: Number?): JsonValue = wrap(value)
        operator fun invoke(value: Boolean?): JsonValue = wrap(value)

        internal inline fun <reified T> wrap(value: T, path: String = PATH_ROOT_MARKER): JsonValue {
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

    open operator fun set(key: String, value: JsonValue) {
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

    open operator fun set(index: Int, value: JsonValue) {
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

    open operator fun plusAssign(value: JsonValue) {
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

    open fun <T> valueOrNull(): T? {
        TODO("not implemented")
    }

    open fun <T> value(): T {
        TODO("not implemented")
    }

    open val isNull: Boolean
        get() = false

    open val orNull: JsonValue?
        get() = this

    open val stringOrNull: String?
        get() = null

    open val string: String
        get() = stringOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a string", path))

    open val intOrNull: Int?
        get() = null

    open val int: Int
        get() = intOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an int", path))

    open val longOrNull: Long?
        get() = null

    open val long: Long
        get() = longOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a long", path))

    open val doubleOrNull: Double?
        get() = null

    open val double: Double
        get() = doubleOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a double", path))

    open val boolOrNull: Boolean?
        get() = null

    open val bool: Boolean
        get() = boolOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a bool", path))

    open val arrayOrNull: List<JsonValue>?
        get() = null

    open val array: List<JsonValue>
        get() = arrayOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an array", path))

    open val objOrNull: Map<String, JsonValue>?
        get() = null

    open val obj: Map<String, JsonValue>
        get() = objOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an object", path))

    fun toJsonString(prettyPrint: Boolean = false, adapter: JsonAdapter = DefaultJsonAdapter): String {
        return adapter.stringify(this, prettyPrint)
    }
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
    override fun copyWithPath(path: String) = JsonObject(content, path)

    override val size: Int
        get() = content.size

    override operator fun get(key: String): JsonValue {
        return content.getOrPut(key) { JsonEmpty(path.expandPath(key)) }
    }

    override operator fun set(key: String, value: JsonValue) {
        val childPath = path.expandPath(key)
        if (value.path == childPath) {
            content[key] = value
        } else {
            content[key] = value.copyWithPath(path.expandPath(key))
        }
    }

    override val objOrNull: Map<String, JsonValue>?
        get() = content
}

class JsonArray(
    internal val content: MutableList<JsonValue>,
    path: String = PATH_ROOT_MARKER
) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonArray(content, path)

    override val size: Int
        get() = content.size

    override operator fun get(index: Int): JsonValue {
        return if (index >= 0 && index < content.size) {
            content[index]
        } else {
            return JsonError(
                JsonValueException("Index $index out of bounds (size: ${content.size})", path),
                path.expandPath(index)
            )
        }
    }

    override operator fun set(index: Int, value: JsonValue) {
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

    override operator fun plusAssign(value: JsonValue) {
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

//class JsonComplex: JsonElement()

class JsonError(private val e: JsonValueException, path: String = PATH_ROOT_MARKER) : JsonValue(path) {
    override fun copyWithPath(path: String) = JsonError(e, path)

    override operator fun get(key: String) = JsonError(e, path.expandPath(key))
    override operator fun get(index: Int) = JsonError(e, path.expandPath(index))

    override fun <T> valueOrNull(): T? = null
    override fun <T> value(): T = throwError(e)

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
    path: String = PATH_ROOT_MARKER
) : JsonValue(path) {
    internal var wrapped: JsonValue? = null

    override fun copyWithPath(path: String) = TODO()

    override val size: Int
        get() = wrapped?.size ?: 0

    private fun materializeAsObject(): JsonValue {
        if (wrapped == null) {
            wrapped = JsonObject(mutableMapOf(), path)
        } else if (wrapped !is JsonObject) {
            throwError(JsonValueException("Element ${wrapped!!::class.simpleName} is not an object", path))
        }

        return wrapped!!
    }

    private fun materializeAsArray(): JsonValue {
        if (wrapped == null) {
            wrapped = JsonArray(mutableListOf(), path)
        } else if (wrapped !is JsonArray) {
            throwError(JsonValueException("Element ${wrapped!!::class.simpleName} is not an array", path))
        }

        return wrapped!!
    }

    override operator fun get(key: String): JsonValue {
        return materializeAsObject()[key]
    }

    override operator fun set(key: String, value: JsonValue) {
        materializeAsObject()[key] = value
    }

    override operator fun get(index: Int): JsonValue {
        return materializeAsArray()[index]
    }

    override operator fun set(index: Int, value: JsonValue) {
        materializeAsArray()[index] = value
    }

    override operator fun plusAssign(value: JsonValue) {
        materializeAsArray() += value
    }

    override val isNull: Boolean
        get() = wrapped == null

    override val orNull: JsonValue?
        get() = wrapped
}