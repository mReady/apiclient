package net.mready.apiclient.builders

import io.ktor.http.*
import io.ktor.http.content.*
import net.mready.json.JsonAdapter
import net.mready.json.JsonObjectDsl

@ApiDsl
/**
 * Builder for a body containing a Json object as root.
 *
 * @sample jsonObjectBodySample
 */
class JsonObjectBodyBuilder(block: JsonObjectDsl.() -> Unit) : RequestBodyBuilder {
    private val objectDsl = JsonObjectDsl()
    val jsonObject = objectDsl.obj

    init {
        objectDsl.apply(block)
    }

    override fun build(adapter: JsonAdapter): TextContent {
        return TextContent(adapter.stringify(jsonObject), ContentType.Application.Json)
    }
}

/**
 * Build Json object body with [JsonObjectBodyBuilder] via [block].
 */
fun jsonObjectBody(block: JsonObjectDsl.() -> Unit): RequestBodyBuilder {
    return JsonObjectBodyBuilder(block)
}