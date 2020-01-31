@file:Suppress("unused")

package net.mready.apiclient

import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun jsonObject(block: JsonObjectBodyBuilder.() -> Unit): RequestBodyBuilder {
    return JsonObjectBodyBuilder().apply(block)
}

fun jsonArray(block: JsonArrayBodyBuilder.() -> Unit): RequestBodyBuilder {
    return JsonArrayBodyBuilder().apply(block)
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

@ApiDsl
class JsonObjectBodyBuilder : RequestBodyBuilder {
    val values = mutableMapOf<String, Any?>()

    infix fun String.value(value: String?) {
        if (value != null) {
            values[this] = value
        }
    }

    infix fun String.value(value: Number?) {
        values[this] = value
    }

    infix fun String.value(value: Boolean) {
        values[this] = value
    }

    infix fun String.obj(block: JsonObjectBodyBuilder.() -> Unit) {
        values[this] = JsonObjectBodyBuilder().apply(block).values
    }

    infix fun String.array(block: JsonArrayBodyBuilder.() -> Unit) {
        values[this] = JsonArrayBodyBuilder().apply(block).values
    }

    override fun build(serializer: JsonSerializer): RequestBody? {
        if (values.isEmpty()) return "".toRequestBody("application/json".toMediaType())

        return serializer.string(values)
            .toRequestBody("application/json".toMediaType())
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
class JsonArrayBodyBuilder : RequestBodyBuilder {
    val values = mutableListOf<Any>()

    fun emmit(value: String) {
        values.add(value)
    }

    fun emmit(value: Number) {
        values.add(value)
    }

    fun emmit(value: Boolean) {
        values.add(value)
    }

    fun emmit(block: JsonObjectBodyBuilder.() -> Unit) {
        values.add(JsonObjectBodyBuilder().apply(block).values)
    }

    override fun build(serializer: JsonSerializer): RequestBody? {
        return serializer.string(values)
            .toRequestBody("application/json".toMediaType())
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

        val part = MultipartBody.Part.createFormData(
            this,
            value.name,
            value.asRequestBody("application/octet-stream".toMediaType())
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