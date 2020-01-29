package net.mready.json.experimental

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.mready.json.*

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

    fun stringify(json: JsonElement, prettyPrint: Boolean = false): String {
        @Suppress("EXPERIMENTAL_API_USAGE")
        val config = JsonConfiguration(
            prettyPrint = prettyPrint,
            //TODO: What is this for?
            useArrayPolymorphism = true
        )
        return Json(config).stringify(JsonElementSerializer, json)
    }

    override fun buildObject(block: JsonObjectDsl.() -> Unit): JsonValue {
        return ExJsonObjectDsl().apply(block).build()
    }

    override fun buildArray(block: JsonArrayDsl.() -> Unit): JsonValue {
        return ExJsonArrayDsl().apply(block).build()
    }
}