package net.mready.json.experimental

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.JsonInput

typealias KJsonElement = kotlinx.serialization.json.JsonElement
typealias KJsonNull = kotlinx.serialization.json.JsonNull
typealias KJsonLiteral = kotlinx.serialization.json.JsonLiteral
typealias KJsonObject = kotlinx.serialization.json.JsonObject
typealias KJsonArray = kotlinx.serialization.json.JsonArray

operator fun JsonElement.invoke(kJsonElement: KJsonElement): JsonElement {
    return convertKJsonElement(kJsonElement)
}

private fun convertKJsonElement(element: KJsonElement, path: String = "[root]"): JsonElement {
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
            is JsonEmpty -> obj.wrapped?.let { serialize(encoder, it) } ?: serialize(encoder, JsonNull(obj.path))
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
    override val descriptor: SerialDescriptor =
        NamedListClassDescriptor("JsonArray", JsonElementSerializer.descriptor)

    override fun serialize(encoder: Encoder, obj: JsonArray) {
        ArrayListSerializer(JsonElementSerializer).serialize(encoder, obj.content)
    }

    override fun deserialize(decoder: Decoder): JsonArray {
        return JsonArray(ArrayListSerializer(JsonElementSerializer).deserialize(decoder).toMutableList())
    }
}

object JsonNullSerializer : KSerializer<JsonNull> {
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

object JsonPrimitiveSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("JsonPrimitive") {
        override val kind: SerialKind = PrimitiveKind.STRING
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