package net.mready.json.experimental

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonInput
import net.mready.json.*


typealias KJsonElement = kotlinx.serialization.json.JsonElement
typealias KJsonPrimitive = kotlinx.serialization.json.JsonPrimitive
typealias KJsonNull = kotlinx.serialization.json.JsonNull
typealias KJsonLiteral = kotlinx.serialization.json.JsonLiteral
typealias KJsonObject = kotlinx.serialization.json.JsonObject
typealias KJsonArray = kotlinx.serialization.json.JsonArray


object ExperimentalJsonAdapter : JsonAdapter {
    override val EMPTY_JSON: JsonValue by lazy { JsonNull() }

    override fun parse(string: String): JsonValue {
        if (string.isBlank()) {
            return EMPTY_JSON
        }

        try {
            @Suppress("EXPERIMENTAL_API_USAGE")
            return Json.parse(JsonElementSerializer, string)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun buildObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return ExJsonObjectDsl().apply(block).build()
    }

    override fun buildArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return ExJsonArrayDsl().apply(block).build()
    }
}


sealed class JsonElement(val path: String) : JsonValue {
    protected fun throwError(e: JsonValueException): Nothing {
        throw JsonValueException(
            message = "JSON error on $path",
            cause = e
        )
    }

    override fun get(key: String): JsonValue {
        return JsonError(
            JsonValueException("Element ${this::class.simpleName} is not an object at $path"),
            "$path > $key"
        )
    }

    override fun get(index: Int): JsonValue {
        return JsonError(
            JsonValueException("Element ${this::class.simpleName} is not an array at $path"),
            "$path > [$index]"
        )
    }

    override fun <T> valueOrNull(): T? {
        TODO("not implemented")
    }

    override fun <T> value(): T {
        TODO("not implemented")
    }

    override val isNull: Boolean
        get() = false

    override val orNull: JsonValue?
        get() = this

    override val stringOrNull: String?
        get() = null

    override val string: String
        get() = stringOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a string at $path"))

    override val intOrNull: Int?
        get() = null

    override val int: Int
        get() = intOrNull ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an int at $path"))

    override val longOrNull: Long?
        get() = null

    override val long: Long
        get() = longOrNull ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a long at $path"))

    override val doubleOrNull: Double?
        get() = null

    override val double: Double
        get() = doubleOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a double at $path"))

    override val boolOrNull: Boolean?
        get() = null

    override val bool: Boolean
        get() = boolOrNull ?: throwError(JsonValueException("Element ${this::class.simpleName} is not a bool at $path"))

    override val arrayOrNull: List<JsonValue>?
        get() = null

    override val array: List<JsonValue>
        get() = arrayOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an array at $path"))

    override val objOrNull: Map<String, JsonValue>?
        get() = null

    override val obj: Map<String, JsonValue>
        get() = objOrNull
            ?: throwError(JsonValueException("Element ${this::class.simpleName} is not an object at $path"))

    override fun toJsonString(prettyPrint: Boolean): String {
        @Suppress("EXPERIMENTAL_API_USAGE")
        val config = JsonConfiguration(
            prettyPrint = prettyPrint,
            //TODO: What is this for?
            useArrayPolymorphism = true
        )
        return Json(config).stringify(JsonElementSerializer, this)
    }
}


class JsonNull(path: String = "[root]") : JsonElement(path) {
    override val isNull: Boolean
        get() = true

    override val orNull: JsonValue?
        get() = null
}

class JsonObject(
    internal val content: MutableMap<String, JsonElement>,
    path: String = "[root]"
) : JsonElement(path) {
    override fun get(key: String): JsonValue {
//        return content.getOrPut(key) { JsonEmpty("$path > $key") }
        return content.getOrPut(key) { JsonNull("$path > $key") }
    }

    override val objOrNull: Map<String, JsonValue>?
        get() = content
}

class JsonArray(
    internal val content: MutableList<JsonElement>,
    path: String = "[root]"
) : JsonElement(path) {
    override fun get(index: Int): JsonValue {
        return if (index >= 0 && index < content.size) {
            content[index]
        } else {
            return JsonError(
                JsonValueException("Index $index out of bounds (size: ${content.size}) at $path"),
                "$path > [$index]"
            )
        }
    }

    override val arrayOrNull: List<JsonValue>?
        get() = content
}

class JsonPrimitive(
    internal val content: String,
    internal val type: Type,
    path: String = "[root]"
) : JsonElement(path) {
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
class JsonError(internal val e: JsonValueException, path: String = "[root]") : JsonElement(path) {
    override fun get(key: String) = JsonError(e, "$path > $key")

    override fun get(index: Int) = JsonError(e, "$path > [$index]")

    override fun <T> valueOrNull() = null

    override fun <T> value() = throwError(e)

    override val isNull: Boolean
        get() = true

    override val orNull: JsonValue?
        get() = null

    override val string: String
        get() = throwError(e)

    override val int: Int
        get() = throwError(e)

    override val long: Long
        get() = throwError(e)

    override val double: Double
        get() = throwError(e)

    override val bool: Boolean
        get() = throwError(e)

    override val array: List<JsonValue>
        get() = throwError(e)

    override val obj: Map<String, JsonValue>
        get() = throwError(e)
}

class JsonEmpty(
    path: String = "[root]"
) : JsonElement(path) {
    internal var content: JsonElement? = null

    operator fun set(key: String, value: JsonElement) {
        val content = this.content
        if (content is JsonObject) {
            content.content[key] = value
            this.content = content
        } else {
            TODO()
        }
    }
}

fun test() {

}

fun convertKJsonElement(element: KJsonElement, path: String = "[root]"): JsonElement {
    return when (element) {
        is KJsonObject -> {
            val content = element.content.mapValuesTo(mutableMapOf()) {
                convertKJsonElement(it.value, "$path > ${it.key}")
            }
            JsonObject(content)
        }
        is KJsonArray -> {
            val content = element.content.mapIndexedTo(mutableListOf()) { index, item ->
                convertKJsonElement(item, "$path > [$index]")
            }
            JsonArray(content)
        }
        is KJsonNull -> JsonNull(path)
        is KJsonLiteral -> JsonPrimitive(
            element.content,
            if (element.isString) JsonPrimitive.Type.STRING else JsonPrimitive.Type.UNKNOWN,
            path
        )
    }
}


object JsonElementSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("JsonElementSerializer") {
        override val kind: SerialKind get() = PolymorphicKind.SEALED
    }

    override fun serialize(encoder: Encoder, obj: JsonElement) {
        when (obj) {
            is JsonPrimitive -> encoder.encodeSerializableValue(JsonPrimitiveSerializer, obj)
            is JsonObject -> encoder.encodeSerializableValue(JsonObjectSerializer, obj)
            is JsonArray -> encoder.encodeSerializableValue(JsonArraySerializer, obj)
            is JsonNull -> encoder.encodeSerializableValue(JsonNullSerializer, obj)
        }
    }

    override fun deserialize(decoder: Decoder): JsonElement {
        val input = decoder as JsonInput
        return convertKJsonElement(input.decodeJson())
    }
}

object JsonObjectSerializer : KSerializer<JsonObject> {
    override val descriptor: SerialDescriptor =
        NamedMapClassDescriptor("JsonObject", StringSerializer.descriptor, JsonElementSerializer.descriptor)

    override fun serialize(encoder: Encoder, obj: JsonObject) {
        LinkedHashMapSerializer(StringSerializer, JsonElementSerializer).serialize(encoder, obj.content)
    }

    override fun deserialize(decoder: Decoder): JsonObject {
        return JsonObject(
            LinkedHashMapSerializer(
                StringSerializer,
                JsonElementSerializer
            ).deserialize(decoder).toMutableMap()
        )
    }
}

object JsonArraySerializer : KSerializer<JsonArray> {
    override val descriptor: SerialDescriptor = NamedListClassDescriptor(
        "JsonArray",
        JsonElementSerializer.descriptor
    )

    override fun serialize(encoder: Encoder, obj: JsonArray) {
        ArrayListSerializer(JsonElementSerializer).serialize(encoder, obj.content)
    }

    override fun deserialize(decoder: Decoder): JsonArray {
        return JsonArray(ArrayListSerializer(JsonElementSerializer).deserialize(decoder).toMutableList())
    }
}

object JsonNullSerializer : KSerializer<JsonNull> {
    override val descriptor: SerialDescriptor get() = JsonNullDescriptor

    override fun serialize(encoder: Encoder, obj: JsonNull) {
        encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): JsonNull {
        decoder.decodeNull()
        return JsonNull()
    }

    private object JsonNullDescriptor : SerialClassDescImpl("JsonNull") {
        // technically, JsonNull is an object, but it does not call beginStructure/endStructure
        override val kind: SerialKind get() = UnionKind.ENUM_KIND
    }
}

object JsonPrimitiveSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor
        get() = object : SerialClassDescImpl("JsonPrimitive") {
            override val kind: SerialKind
                get() = PrimitiveKind.STRING
        }

    override fun serialize(encoder: Encoder, obj: JsonPrimitive) {
        when (obj.type) {
            JsonPrimitive.Type.NUMBER -> obj.longOrNull?.let { encoder.encodeLong(it) }
                ?: encoder.encodeDouble(obj.double)
            JsonPrimitive.Type.BOOLEAN -> encoder.encodeBoolean(obj.bool)
            JsonPrimitive.Type.STRING -> encoder.encodeString(obj.string)
            JsonPrimitive.Type.UNKNOWN -> encoder.encodeString(obj.toString())
        }
    }

    override fun deserialize(decoder: Decoder): JsonPrimitive {
        return JsonPrimitive(decoder.decodeString(), JsonPrimitive.Type.UNKNOWN)
    }
}