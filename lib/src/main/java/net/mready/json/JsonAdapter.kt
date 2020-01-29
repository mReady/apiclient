package net.mready.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.lang.RuntimeException

class JsonParseException(message: String, cause: Throwable): RuntimeException(message, cause)

interface JsonAdapter {
    fun parse(string: String): JsonValue
    fun stringify(json: JsonValue, prettyPrint: Boolean = false): String
}

object DefaultJsonAdapter : JsonAdapter {
    override fun parse(string: String): JsonValue {
        if (string.isBlank()) {
            return JsonEmpty()
        }

        try {
            @Suppress("EXPERIMENTAL_API_USAGE")
            return Json.parse(JsonValueSerializer, string)
        } catch (e: Throwable) {
            throw JsonParseException(e.message ?: "Unable to parse JSON", e)
        }
    }

    override fun stringify(json: JsonValue, prettyPrint: Boolean): String {
        @Suppress("EXPERIMENTAL_API_USAGE")
        val config = JsonConfiguration(
            prettyPrint = prettyPrint,
            //TODO: What is this for?
            useArrayPolymorphism = true
        )
        return Json(config).stringify(JsonValueSerializer, json)
    }
}