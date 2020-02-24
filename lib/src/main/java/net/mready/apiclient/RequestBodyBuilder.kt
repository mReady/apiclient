@file:Suppress("unused")

package net.mready.apiclient

import net.mready.json.Json
import net.mready.json.JsonAdapter
import net.mready.json.JsonArrayDsl
import net.mready.json.JsonObjectDsl
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.nio.file.Files

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
    fun build(adapter: JsonAdapter): RequestBody?
}

@DslMarker
annotation class ApiDsl

@ApiDsl
class RawBodyBuilder(private val content: String?) : RequestBodyBuilder {
    override fun build(adapter: JsonAdapter): RequestBody? {
        return content?.toRequestBody("text/plain".toMediaType())
    }
}

@ApiDsl
class JsonObjectBodyBuilder(private val block: JsonObjectDsl.() -> Unit) : RequestBodyBuilder {
    private val objectDsl = JsonObjectDsl()
    val jsonObject = objectDsl.obj

    init {
        objectDsl.apply(block)
    }

    override fun build(adapter: JsonAdapter): RequestBody? {
        return jsonObject.toJsonString(adapter = adapter).toRequestBody("application/json".toMediaType())
    }
}

@ApiDsl
class JsonArrayBodyBuilder(private val block: JsonArrayDsl.() -> Unit) : RequestBodyBuilder {
    private val arrayDsl = JsonArrayDsl()
    val jsonArray = arrayDsl.array

    init {
        arrayDsl.apply(block)
    }

    override fun build(adapter: JsonAdapter): RequestBody? {
        return jsonArray.toJsonString(adapter = adapter).toRequestBody("application/json".toMediaType())
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

    override fun build(adapter: JsonAdapter): RequestBody? {
        if (values.isEmpty()) return null

        return FormBody.Builder().apply {
            values.forEach { (key, value) ->
                when (value) {
                    is Json -> add(key, adapter.stringify(value))
                    else -> add(key, value.toString())
                }
            }
        }.build()
    }
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

    infix fun String.file(value: File) {
        values.add(this to value)
    }

    infix fun String.value(value: Json?) {
        values.add(this to value)
    }

    override fun build(adapter: JsonAdapter): RequestBody? {
        if (values.isEmpty()) return null

        return MultipartBody.Builder()
            .setType("multipart/form-data".toMediaType())
            .apply {
                values.forEach { (key, value) ->
                    val part = when (value) {
                        is File -> {
                            val mimeType = runCatching { Files.probeContentType(value.toPath()) }.getOrNull()
                                ?: "application/octet-stream"

                            MultipartBody.Part.createFormData(
                                name = key,
                                filename = value.name,
                                body = value.asRequestBody(mimeType.toMediaType())
                            )
                        }
                        is Json -> {
                            MultipartBody.Part.createFormData(
                                name = key,
                                value = adapter.stringify(value)
                            )
                        }
                        else -> {
                            MultipartBody.Part.createFormData(
                                name = key,
                                value = value.toString()
                            )
                        }
                    }
                    addPart(part)
                }
            }.build()
    }
}