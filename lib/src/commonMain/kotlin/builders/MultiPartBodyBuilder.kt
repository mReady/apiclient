package builders

import io.ktor.client.request.forms.*
import io.ktor.http.*
import net.mready.json.Json
import net.mready.json.JsonAdapter


/**
 * Represents a file to be sent in a multipart form data.
 *
 * @param byteArray The content of the file.
 * @param fileName The name of the file.
 * @param contentType The content type of the file. Defaults to [FileInfo.Companion.defaultContentType]
 * @param contentLength The length of the content.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class FileInfo {
    val byteArray: ByteArray
    val fileName: String
    val contentType: ContentType
    val contentLength: Long

    companion object {
        internal fun create(
            byteArray: ByteArray,
            fileName: String,
            contentType: ContentType = FileInfo.defaultContentType
        ): FileInfo
    }
}

/**
 * Default content type for [FileInfo] which is [ContentType.Application.OctetStream].
 */
val FileInfo.Companion.defaultContentType: ContentType
    get() = ContentType.Application.OctetStream


@ApiDsl
/**
 * Builder for a body containing a multipart form data.
 *
 * For [FileInfo] if [FileInfo.contentType] is not provided, it will be guessed from the [FileInfo.byteArray].
 * If the content type cannot be guessed, [ContentType.Application.OctetStream] will be used.
 *
 * @sample multipartBodySample
 */
class MultiPartBodyBuilder : RequestBodyBuilder {

    val values = mutableListOf<Pair<String, Any?>>() //Any: String | Number | Boolean | Json | File

    /**
     * Assign a string [value] to a string key in the form data.
     */
    infix fun String.value(value: String?) {
        values.add(this to value)
    }

    /**
     * Assign a number [value] to a string key in the form data.
     */
    infix fun String.value(value: Number?) {
        values.add(this to value)
    }

    /**
     * Assign a boolean [value] to a string key in the form data.
     */
    infix fun String.value(value: Boolean?) {
        values.add(this to value)
    }

    /**
     * Assign a [FileInfo] [value] to a string key in the form data.
     */
    infix fun String.file(value: FileInfo) {
        values.add(this to value)
    }

    /**
     * Assign a [Json] [value] to a string key in the form data.
     */
    infix fun String.value(value: Json?) {
        values.add(this to value)
    }

    override fun build(adapter: JsonAdapter): MultiPartFormDataContent? {
        if (values.isEmpty()) return null

        return MultiPartFormDataContent(
            parts = formData {
                values.forEach { (key, value) ->
                    when (value) {
                        is FileInfo -> {
                            append(key = key, value = value.byteArray, headers = Headers.build {
                                append(
                                    name = HttpHeaders.ContentType,
                                    value = value.contentType.toString()
                                )
                                append(
                                    name = HttpHeaders.ContentDisposition,
                                    value = "filename=${value.fileName.escapeIfNeeded()}"
                                )
                            })
                        }

                        is Json -> {
                            append(key, adapter.stringify(value))
                        }

                        is Number -> {
                            append(key, value)
                        }

                        is Boolean -> {
                            append(key, value)
                        }

                        else -> {
                            append(key, value.toString())
                        }
                    }
                }
            })
    }
}

/**
 * Build multipart form body with [MultiPartBodyBuilder] via [block].
 */
fun multipartBody(block: MultiPartBodyBuilder.() -> Unit): RequestBodyBuilder {
    return MultiPartBodyBuilder().apply(block)
}