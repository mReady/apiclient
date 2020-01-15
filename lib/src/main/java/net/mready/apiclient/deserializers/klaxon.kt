package net.mready.apiclient.deserializers

import com.beust.klaxon.Klaxon
import net.mready.apiclient.JsonElement
import net.mready.apiclient.JsonInvalidElement
import net.mready.apiclient.JsonParseException
import net.mready.apiclient.JsonSerializer

class KlaxonJsonSerializer(private val klaxon: Klaxon) :
    JsonSerializer {
    override fun string(value: Any?): String {
        return klaxon.toJsonString(value)
    }

    override fun parse(json: String): JsonElement {
        val jsonString = json.trim()
        try {
            return JsonElement(
                when {
                    jsonString.startsWith('{') -> klaxon.parseJsonObject(jsonString.reader())
                    jsonString.startsWith('[') -> klaxon.parseJsonArray(jsonString.reader())
                    jsonString.isEmpty() -> JsonInvalidElement("The JSON body is empty")
                    else -> throw JsonParseException("Unable to parse JSON string")
                }
            )
        } catch (e: Throwable) {
            throw JsonParseException(
                "Unable to parse JSON",
                e
            )
        }
    }
}