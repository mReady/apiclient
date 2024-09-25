package builders

import io.ktor.http.*
import io.ktor.http.content.*
import net.mready.json.JsonAdapter

@ApiDsl
/**
 * Builder for a body containing a raw string.
 *
 * @sample rawBodySample
 */
class RawBodyBuilder(private val content: String?) : RequestBodyBuilder {
    override fun build(adapter: JsonAdapter): TextContent? {
        return content?.let { TextContent(it, ContentType.Text.Plain) }
    }
}

/**
 * Convenience function to build raw body with [RawBodyBuilder].
 */
fun rawBody(content: String?): RequestBodyBuilder {
    return RawBodyBuilder(content)
}