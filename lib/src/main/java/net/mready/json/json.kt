package net.mready.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElementSerializer
import kotlinx.serialization.json.json
import net.mready.json.kotlinx.KotlinxJsonValue

interface JsonDeserializer {
    fun parse(string: String): JsonValue
}

fun parseJson(string: String, deserializer: JsonDeserializer = KotlinxJsonValue): JsonValue {
    return deserializer.parse(string)
}

interface JsonValue {
    operator fun get(key: String): JsonValue
    operator fun get(index: Int): JsonValue

    fun isNull(): Boolean
    fun orNull(): JsonValue?

    fun stringOrNull(): String?
    fun string(): String

    fun intOrNull(): Int?
    fun int(): Int

    fun longOrNull(): Long?
    fun long(): Long

    fun doubleOrNull(): Double?
    fun double(): Double

    fun boolOrNull(): Boolean?
    fun bool(): Boolean

    fun arrayOrNull(): List<JsonValue>?
    fun array(): List<JsonValue>

    fun objOrNull(): Map<String, JsonValue>?
    fun obj(): Map<String, JsonValue>

    fun toJsonString(): String
}


class KJsonElement(
    private val value: KJsonElementValue,
    private val path: List<String>
) {
    operator fun get(key: String): KJsonElement {
        val childPath = path + key

        if (value is KJsonError) {
            return KJsonElement(value, childPath)
        }

        if (value is KJsonObject) {
            return KJsonElement(value[key] ?: KJsonNull, childPath)
        } else {
            return KJsonElement(KJsonError(), childPath)
        }
    }
}


sealed class KJsonElementValue
object KJsonNull : KJsonElementValue()
class KJsonPrimitive : KJsonElementValue() {
    enum class Kind {
        STRING, NUMBER, BOOLEAN
    }
}

class KJsonObject(private val content: Map<String, KJsonElementValue>) : KJsonElementValue(),
    Map<String, KJsonElementValue> by content

class KJsonArray(private val content: List<KJsonElementValue>) : KJsonElementValue(), List<KJsonElementValue> by content
class KJsonError : KJsonElementValue()


/*

val json = jsonObject {
    "hello" += "world"
    "array" += array {
        emit(1)
    }
}

val json = JsonObject()
json["hello"] = "world"
json["array"][0] = 1


val str = json["foo"]["bar"].string()


*/





fun main() {
    val obj = json {
        "test" to 123
    }

    val str = Json.stringify(JsonElementSerializer, obj)
    println(str)

    val obj2 = Json.parse(JsonElementSerializer, str)
    println(obj)

    println(Json.stringify(JsonElementSerializer, obj2))
}