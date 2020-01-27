@file:Suppress("unused")

package net.mready.apiclient

import net.mready.json.JsonArrayDsl
import net.mready.json.JsonObjectDsl
import net.mready.json.JsonValue
import net.mready.json.kotlinx.KotlinxJsonObjectDsl
import net.mready.json.kotlinx.jsonArray
import net.mready.json.kotlinx.jsonObject
import okhttp3.FormBody
import okhttp3.MediaType
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
    fun build(serializer: JsonSerializer): RequestBody?
}

@DslMarker
annotation class ApiDsl

class RawBodyBuilder(private val content: String?): RequestBodyBuilder {
    override fun build(serializer: JsonSerializer): RequestBody? {
        return content?.toRequestBody("text/plain".toMediaType())
    }
}

@ApiDsl
class JsonObjectBodyBuilder(block: JsonObjectDsl.() -> Unit) : RequestBodyBuilder {
    private val value: JsonValue = jsonObject(block)

    override fun build(serializer: JsonSerializer): RequestBody? {
        return value.toJsonString().toRequestBody("application/json".toMediaType())
    }
}

@ApiDsl
class JsonArrayBodyBuilder(block: JsonArrayDsl.() -> Unit) : RequestBodyBuilder {
    val value: JsonValue = jsonArray(block)

    override fun build(serializer: JsonSerializer): RequestBody? {
        return value.toJsonString().toRequestBody("application/json".toMediaType())
    }
}

@ApiDsl
class FormBodyBuilder : RequestBodyBuilder {
    val values = mutableListOf<Pair<String, Any?>>()

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

    infix fun String.value(value: Boolean) {
        values.add(this to value)
    }

    override fun build(serializer: JsonSerializer): RequestBody? {
        if (values.isEmpty()) return FormBody.Builder().build()

        return FormBody.Builder().apply {
            values.forEach { (key, value) ->
                add(key, value.toString())
            }
        }.build()
    }
}

@ApiDsl
class MultiPartBodyBuilder : RequestBodyBuilder {
    enum class MultipartBodyType {
        FORM, ALTERNATIVE, DIGEST, MIXED, PARALLEL
    }

    var mode = MultipartBodyType.FORM

    val values = mutableListOf<MultipartBody.Part>()

    infix fun String.value(value: String?) {
        if (value != null) {
            val part = MultipartBody.Part.createFormData(
                this,
                value.toString()
            )
            values.add(part)
        }
    }

    infix fun String.value(value: Number?) {
        if (value != null) {
            val part = MultipartBody.Part.createFormData(
                this,
                value.toString()
            )
            values.add(part)
        }
    }

    infix fun String.value(value: Boolean) {
        val part = MultipartBody.Part.createFormData(
            this,
            value.toString()
        )
        values.add(part)
    }

    infix fun String.file(value: File) {
        val mimeType = Files.probeContentType(value.toPath())

        val part = MultipartBody.Part.createFormData(
            this,
            value.name,
            value.asRequestBody((mimeType ?: "application/octet-stream").toMediaType())
        )
        values.add(part)
    }

    override fun build(serializer: JsonSerializer): RequestBody? {
        if (values.isEmpty()) return null

        return MultipartBody
            .Builder()
            .setType(getMultipartBodyType(mode))
            .apply {
                values.forEach { addPart(it) }
            }.build()
    }

    private fun getMultipartBodyType(mode: MultipartBodyType): MediaType {
        return when (mode) {
            MultipartBodyType.FORM -> "multipart/form-data".toMediaType()
            MultipartBodyType.ALTERNATIVE -> "multipart/alternative".toMediaType()
            MultipartBodyType.DIGEST -> "multipart/digest".toMediaType()
            MultipartBodyType.MIXED -> "multipart/mixed".toMediaType()
            MultipartBodyType.PARALLEL -> "multipart/alternative".toMediaType()
        }
    }
}