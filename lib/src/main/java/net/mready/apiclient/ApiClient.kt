@file:Suppress("unused")

package net.mready.apiclient

import com.beust.klaxon.Klaxon
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mready.apiclient.deserializers.KlaxonJsonSerializer
import net.mready.json.JsonValue
import net.mready.json.parseJson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpCodeException(val code: Int, message: String) : RuntimeException(message)
class ParseException(message: String) : RuntimeException(message)
class ApiException(message: String) : RuntimeException(message)


typealias ResponseProcessor<T> = (JsonValue) -> T

enum class Method(val allowBody: Boolean) {
    GET(false), POST(true), PUT(true), DELETE(true)
}

open class ApiClient(
    private val baseUrl: String = "",
    protected val httpClient: OkHttpClient = OkHttpClient(),
    protected val jsonSerializer: JsonSerializer = KlaxonJsonSerializer(Klaxon())
) {

    fun buildUrl(endpoint: String, query: Map<String, Any?>? = null): String {
        val url = if (endpoint.startsWith("http")) {
            endpoint
        } else {
            baseUrl.trimEnd('/') + "/" + endpoint.trimStart('/')
        }

        return if (query != null) {
            url.toHttpUrlOrNull()!!.newBuilder()
                .apply {
                    query.forEach { (key, value) ->
                        if (value != null) {
                            addQueryParameter(key, value.toString())
                        }
                    }
                }.build().toString()
        } else {
            url
        }
    }

    protected open fun prepareRequestBody(builder: RequestBodyBuilder): RequestBody? {
        return builder.build(jsonSerializer)
    }

    protected open fun prepareRequest(builder: Request.Builder): Request {
        return builder.build()
    }

    protected open fun checkResponse(response: Response, json: JsonValue) {
    }

    protected open fun checkErrorResponse(response: Response, json: JsonValue) {
    }

    protected open suspend fun makeRequest(request: Request): Response {
        return httpClient.newCall(request).await()
    }

    protected open fun parseResponse(body: String): JsonValue {
        return parseJson(body)
    }

    suspend fun <T> get(
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        errorProcessor: ResponseProcessor<Unit>? = null,
        response: ResponseProcessor<T>
    ): T {
        return call(
            method = Method.GET,
            endpoint = endpoint,
            query = query,
            headers = headers,
            errorProcessor = errorProcessor,
            response = response
        )
    }

    suspend fun <T> post(
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        body: RequestBodyBuilder? = null,
        errorProcessor: ResponseProcessor<Unit>? = null,
        response: ResponseProcessor<T>
    ): T {
        return call(
            method = Method.POST,
            endpoint = endpoint,
            query = query,
            headers = headers,
            body = body,
            errorProcessor = errorProcessor,
            response = response
        )
    }

    suspend fun <T> put(
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        body: RequestBodyBuilder? = null,
        errorProcessor: ResponseProcessor<Unit>? = null,
        response: ResponseProcessor<T>
    ): T {
        return call(
            method = Method.PUT,
            endpoint = endpoint,
            query = query,
            headers = headers,
            body = body,
            errorProcessor = errorProcessor,
            response = response
        )
    }

    suspend fun <T> delete(
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        body: RequestBodyBuilder? = null,
        errorProcessor: ResponseProcessor<Unit>? = null,
        response: ResponseProcessor<T>
    ): T {
        return call(
            method = Method.DELETE,
            endpoint = endpoint,
            query = query,
            headers = headers,
            body = body,
            errorProcessor = errorProcessor,
            response = response
        )
    }

    suspend fun <T> call(
        method: Method = Method.GET,
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        body: RequestBodyBuilder? = null,
        errorProcessor: ResponseProcessor<Unit>? = null,
        response: ResponseProcessor<T>
    ): T {
        val requestBody: RequestBody? = if (method.allowBody) {
            if (body != null) {
                prepareRequestBody(body)
            } else {
                "".toRequestBody()
            }
        } else {
            null
        }

        val url = buildUrl(endpoint, query)

        val request = Request.Builder()
            .url(url)
            .method(method.toString(), requestBody)
            .run {
                headers?.forEach {
                    addHeader(it.key, it.value)
                }
                prepareRequest(this)
            }

        try {
            val networkResponse = makeRequest(request)
            if (networkResponse.isSuccessful) {
                val responseJson = try {
                    networkResponse.body!!.use {
                        parseResponse(it.string())
                    }
                } catch (e: Throwable) {
                    throw ParseException("Unable to parse request body for $endpoint").initCause(e)
                }

                checkResponse(networkResponse, responseJson)
                return response(responseJson)
            } else {
                val statusCode = networkResponse.code
                val responseBody = networkResponse.body

                if (responseBody != null && responseBody.contentLength() != 0L) {
                    runCatching { parseResponse(responseBody.string()) }
                        .onSuccess {
                            errorProcessor?.invoke(it)
                            checkErrorResponse(networkResponse, it)
                        }
                }

                throw HttpCodeException(
                    statusCode,
                    networkResponse.message
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

//currently OkHttp doesn't have an await for Call, maybe in the future will have and this will not be needed anymore
suspend fun Call.await() = suspendCancellableCoroutine<Response> { c ->
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            c.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            if (!c.isCancelled) {
                c.resumeWithException(e)
            }
        }
    })

    c.invokeOnCancellation {
        if (c.isCancelled)
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
    }
}