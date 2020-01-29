package net.mready.json

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.JsonInput

private typealias KJsonElement = kotlinx.serialization.json.JsonElement
private typealias KJsonNull = kotlinx.serialization.json.JsonNull
private typealias KJsonLiteral = kotlinx.serialization.json.JsonLiteral
private typealias KJsonObject = kotlinx.serialization.json.JsonObject
private typealias KJsonArray = kotlinx.serialization.json.JsonArray

fun JsonValue.Companion.from(jsonElement: KJsonElement): JsonValue {
    return convertJsonElement(jsonElement)
}

private fun convertJsonElement(element: KJsonElement, path: String = PATH_ROOT_MARKER): JsonValue {
    return when (element) {
        is KJsonObject -> {
            val content = element.content.mapValuesTo(mutableMapOf()) {
                convertJsonElement(it.value, path.expandPath(it.key))
            }
            JsonObject(content)
        }
        is KJsonArray -> {
            val content = element.content.mapIndexedTo(mutableListOf()) { index, item ->
                convertJsonElement(item, path.expandPath(index))
            }
            JsonArray(content)
        }
        is KJsonNull -> JsonNull(path)
        is KJsonLiteral -> JsonPrimitive(
            content = element.content,
            type = if (element.isString) JsonPrimitive.Type.STRING else JsonPrimitive.Type.UNKNOWN,
            path = path
        )
    }
}


object JsonValueSerializer : KSerializer<JsonValue> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("JsonValueSerializer") {
        override val kind: SerialKind get() = PolymorphicKind.SEALED
    }

    override fun serialize(encoder: Encoder, obj: JsonValue) {
        when (obj) {
            is JsonPrimitive -> encoder.encodeSerializableValue(JsonPrimitiveSerializer, obj)
            is JsonObject -> encoder.encodeSerializableValue(JsonObjectSerializer, obj)
            is JsonArray -> encoder.encodeSerializableValue(JsonArraySerializer, obj)
            is JsonNull -> encoder.encodeSerializableValue(JsonNullSerializer, obj)
            is JsonEmpty -> obj.wrapped?.let {
                serialize(
                    encoder,
                    it
                )
            } ?: serialize(encoder, JsonNull(obj.path))
        }
    }

    override fun deserialize(decoder: Decoder): JsonValue {
        val input = decoder as JsonInput
        return convertJsonElement(input.decodeJson())
    }
}

private object JsonObjectSerializer : KSerializer<JsonObject> {
    override val descriptor: SerialDescriptor =
        NamedMapClassDescriptor("JsonObject", StringSerializer.descriptor,
            JsonValueSerializer.descriptor
        )

    override fun serialize(encoder: Encoder, obj: JsonObject) {
        LinkedHashMapSerializer(StringSerializer, JsonValueSerializer).serialize(encoder, obj.content)
    }

    override fun deserialize(decoder: Decoder): JsonObject {
        return JsonObject(
            LinkedHashMapSerializer(StringSerializer, JsonValueSerializer)
                .deserialize(decoder)
                .toMutableMap()
        )
    }
}

private object JsonArraySerializer : KSerializer<JsonArray> {
    override val descriptor: SerialDescriptor =
        NamedListClassDescriptor("JsonArray", JsonValueSerializer.descriptor)

    override fun serialize(encoder: Encoder, obj: JsonArray) {
        ArrayListSerializer(JsonValueSerializer).serialize(encoder, obj.content)
    }

    override fun deserialize(decoder: Decoder): JsonArray {
        return JsonArray(ArrayListSerializer(JsonValueSerializer).deserialize(decoder).toMutableList())
    }
}

private object JsonNullSerializer : KSerializer<JsonNull> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("JsonNull") {
        override val kind: SerialKind = UnionKind.ENUM_KIND
    }

    override fun serialize(encoder: Encoder, obj: JsonNull) {
        encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): JsonNull {
        decoder.decodeNull()
        return JsonNull()
    }
}

private object JsonPrimitiveSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("JsonPrimitive") {
        override val kind: SerialKind = PrimitiveKind.STRING
    }

    override fun serialize(encoder: Encoder, obj: JsonPrimitive) {
        when (obj.type) {
            JsonPrimitive.Type.NUMBER -> obj.longOrNull?.let { encoder.encodeLong(it) }
                ?: encoder.encodeDouble(obj.double)
            JsonPrimitive.Type.BOOLEAN -> encoder.encodeBoolean(obj.bool)
            JsonPrimitive.Type.STRING, JsonPrimitive.Type.UNKNOWN -> encoder.encodeString(obj.string)
        }
    }

    override fun deserialize(decoder: Decoder): JsonPrimitive {
        return JsonPrimitive(decoder.decodeString(), JsonPrimitive.Type.UNKNOWN)
    }
}