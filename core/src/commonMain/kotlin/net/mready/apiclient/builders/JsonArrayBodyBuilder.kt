package net.mready.apiclient.builders

import io.ktor.http.*
import io.ktor.http.content.*
import net.mready.json.JsonAdapter
import net.mready.json.JsonArrayDsl

@ApiDsl
/**
 * Builder for a body containing a Json array as root.
 *
 * @sample jsonArrayBodySample
 */
class JsonArrayBodyBuilder(block: JsonArrayDsl.() -> Unit) : RequestBodyBuilder {
    private val arrayDsl = JsonArrayDsl()
    val jsonArray = arrayDsl.array

    init {
        arrayDsl.apply(block)
    }

    override fun build(adapter: JsonAdapter): TextContent {
        return TextContent(adapter.stringify(jsonArray), ContentType.Application.Json)
    }
}

/**
 * Build Json array body with [JsonArrayBodyBuilder] via [block].
 */
fun jsonArrayBody(block: JsonArrayDsl.() -> Unit): RequestBodyBuilder {
    return JsonArrayBodyBuilder(block)
}