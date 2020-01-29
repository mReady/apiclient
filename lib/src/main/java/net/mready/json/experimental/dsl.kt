package net.mready.json.experimental

import net.mready.json.JsonArrayDsl
import net.mready.json.JsonDsl
import net.mready.json.JsonObjectDsl
import net.mready.json.JsonValue

internal open class ExJsonDsl(protected val path: String = PATH_ROOT_MARKER): JsonDsl {
    override fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return ExJsonArrayDsl(path).apply(block).build()
    }

    override fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return ExJsonObjectDsl(path).apply(block).build()
    }
}

internal class ExJsonObjectDsl(path: String = PATH_ROOT_MARKER) : ExJsonDsl(path), JsonObjectDsl {
    private val content = mutableMapOf<String, JsonElement>()

    override fun String.value(value: Nothing?) {
        content[this] = JsonNull(path.expandPath(this))
    }

    override fun String.value(value: String?) {
        content[this] = JsonElement.wrap(value, path.expandPath(this))
    }

    override fun String.value(value: Number?) {
        content[this] = JsonElement.wrap(value, path.expandPath(this))
    }

    override fun String.value(value: Boolean?) {
        content[this] = JsonElement.wrap(value, path.expandPath(this))
    }

    override fun String.value(value: JsonValue?) {
        content[this] = when (value) {
            null -> JsonNull()
            is JsonElement -> value
            else -> throw IllegalArgumentException("JsonValue must be built with the same adapter")
        }
    }

    override infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit) {
        content[this] = ExJsonArrayDsl(path.expandPath(this)).apply(block).build()
    }

    override infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit) {
        content[this] = ExJsonObjectDsl(path.expandPath(this)).apply(block).build()
    }

    internal fun build(): JsonElement {
        return JsonObject(content, path)
    }
}

internal class ExJsonArrayDsl(path: String = PATH_ROOT_MARKER) : ExJsonDsl(path), JsonArrayDsl {
    private val content: MutableList<JsonElement> = mutableListOf()

    override val array = JsonArrayDsl.ArrayItemsCollector

    override fun emit(value: Nothing?) {
        content += JsonNull(path.expandPath(content.size))
    }

    override fun emit(value: String?) {
        content += JsonElement.wrap(value, path.expandPath(content.size))
    }

    override fun emit(value: Number?) {
        content += JsonElement.wrap(value, path.expandPath(content.size))
    }

    override fun emit(value: Boolean?) {
        content += JsonElement.wrap(value, path.expandPath(content.size))
    }

    override fun emit(value: JsonValue?) {
        content += when (value) {
            null -> JsonNull()
            is JsonElement -> value
            else -> throw IllegalArgumentException("JsonValue must be built with the same adapter")
        }
    }

    internal fun build(): JsonElement {
        return JsonArray(content, path)
    }
}