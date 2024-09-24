@file:Suppress("unused")

package net.mready.apiclient

import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import net.mready.json.Json
import net.mready.json.JsonAdapter
import net.mready.json.JsonArrayDsl
import net.mready.json.JsonObjectDsl

fun rawBody(content: String?): RequestBodyBuilder {
    return RawBodyBuilder(content)
}

fun jsonObjectBody(block: JsonObjectDsl.() -> Unit): RequestBodyBuilder {
    return JsonObjectBodyBuilder(block)
}

fun jsonArrayBody(block: JsonArrayDsl.() -> Unit): RequestBodyBuilder {
    return JsonArrayBodyBuilder(block)
}

fun formBody(block: FormBodyBuilder.() -> Unit): RequestBodyBuilder {
    return FormBodyBuilder().apply(block)
}

fun multipartBody(block: MultiPartBodyBuilder.() -> Unit): RequestBodyBuilder {
    return MultiPartBodyBuilder().apply(block)
}


interface RequestBodyBuilder {
    fun build(adapter: JsonAdapter): OutgoingContent?
}

@DslMarker
annotation class ApiDsl

@ApiDsl
class RawBodyBuilder(private val content: String?) : RequestBodyBuilder {
    override fun build(adapter: JsonAdapter): TextContent? {
        return content?.let { TextContent(it, ContentType.Text.Plain) }
    }
}

@ApiDsl
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

@ApiDsl
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


@ApiDsl
class FormBodyBuilder : RequestBodyBuilder {
    val values = mutableListOf<Pair<String, Any?>>() //Any: String | Number | Boolean | Json

    infix fun String.value(value: String?) {
        values.add(this to value)
    }

    infix fun String.value(value: Number?) {
        values.add(this to value)
    }

    infix fun String.value(value: Boolean?) {
        values.add(this to value)
    }

    infix fun String.value(value: Json?) {
        values.add(this to value)
    }

    override fun build(adapter: JsonAdapter): FormDataContent? {
        if (values.isEmpty()) return null

        return FormDataContent(formData = parameters {
            values.forEach { (key, value) ->
                when (value) {
                    is Json -> append(key, adapter.stringify(value))
                    is String -> append(key, value)
                    else -> append(key, value.toString())
                }
            }
        })
    }
}

class FileInfo(
    val byteArray: ByteArray,
    val fileName: String
) {
    val contentLength: Long = byteArray.size.toLong()
}

@ApiDsl
class MultiPartBodyBuilder : RequestBodyBuilder {

    val values = mutableListOf<Pair<String, Any?>>() //Any: String | Number | Boolean | Json | File

    infix fun String.value(value: String?) {
        values.add(this to value)
    }

    infix fun String.value(value: Number?) {
        values.add(this to value)
    }

    infix fun String.value(value: Boolean?) {
        values.add(this to value)
    }

    infix fun String.file(value: FileInfo) {
        values.add(this to value)
    }

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
                                    value = ContentType.Application.OctetStream.toString()
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