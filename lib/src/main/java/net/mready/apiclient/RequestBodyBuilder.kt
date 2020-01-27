@file:Suppress("unused")

package net.mready.apiclient

import net.mready.json.*
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
    override fun build(adapter: JsonAdapter): RequestBody? {
        return jsonObject(adapter, block).toJsonString().toRequestBody("application/json".toMediaType())
    }
}

@ApiDsl
class JsonArrayBodyBuilder(private val block: JsonArrayDsl.() -> Unit) : RequestBodyBuilder {
    override fun build(adapter: JsonAdapter): RequestBody? {
        return jsonArray(adapter, block).toJsonString().toRequestBody("application/json".toMediaType())
    }
}

@ApiDsl
class FormBodyBuilder : RequestBodyBuilder {
    private val values = mutableListOf<Pair<String, Any?>>()

    infix fun String.value(value: String?) {
        if (value != null) {
            values.add(this to value)
        }
    }

    infix fun String.value(value: Number?) {
        if (value != null) {
            values.add(this to value)
        }
    }

    infix fun String.value(value: Boolean?) {
        values.add(this to value)
    }

    override fun build(adapter: JsonAdapter): RequestBody? {
        if (values.isEmpty()) return null

        return FormBody.Builder().apply {
            values.forEach { (key, value) ->
                add(key, value.toString())
            }
        }.build()
    }
}

@ApiDsl
class MultiPartBodyBuilder : RequestBodyBuilder {
    private val values = mutableListOf<MultipartBody.Part>()

    infix fun String.value(value: String?) {
        if (value != null) {
            val part = MultipartBody.Part.createFormData(
                name = this,
                value = value.toString()
            )
            values.add(part)
        }
    }

    infix fun String.value(value: Number?) {
        if (value != null) {
            val part = MultipartBody.Part.createFormData(
                name = this,
                value = value.toString()
            )
            values.add(part)
        }
    }

    infix fun String.value(value: Boolean?) {
        val part = MultipartBody.Part.createFormData(
            name = this,
            value = value.toString()
        )
        values.add(part)
    }

    infix fun String.file(value: File) {
        val mimeType = runCatching { Files.probeContentType(value.toPath()) }.getOrNull() ?: "application/octet-stream"

        val part = MultipartBody.Part.createFormData(
            name = this,
            filename = value.name,
            body = value.asRequestBody(mimeType.toMediaType())
        )
        values.add(part)
    }

    override fun build(adapter: JsonAdapter): RequestBody? {
        if (values.isEmpty()) return null

        return MultipartBody.Builder()
            .setType("multipart/form-data".toMediaType())
            .apply {
                values.forEach { addPart(it) }
            }.build()
    }
}