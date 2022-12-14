@file:Suppress("unused")

package net.mready.apiclient

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mready.json.Json
import net.mready.json.JsonAdapter
import net.mready.json.adapters.KotlinxJsonAdapter
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod

class HttpCodeException(val code: Int, message: String) : RuntimeException(message)
class ParseException(message: String, cause: Throwable?) : RuntimeException(message, cause)

class ApiException(message: String) : RuntimeException(message)

typealias ResponseHandler<T> = (Json) -> T

enum class Method {
    GET, POST, PUT, DELETE
}

open class ApiClient(
    private val baseUrl: String = "",
    protected val httpClient: OkHttpClient = OkHttpClient(),
    protected val jsonAdapter: JsonAdapter = KotlinxJsonAdapter()
) {

    fun buildUrl(endpoint: String, query: Map<String, Any?>? = null): String {
        val url = if (endpoint.startsWith("http")) {
            endpoint
        } else if (endpoint.startsWith("/")) {
            val baseHttpUrl = baseUrl.toHttpUrl()
            val urlBuilder = baseHttpUrl.newBuilder()

            if (baseHttpUrl.pathSegments.isNotEmpty()) {
                urlBuilder.removePathSegment(0)
            }

            urlBuilder.addPathSegments(endpoint.trimStart('/')).build().toUri().toString()
        } else {
            baseUrl.trimEnd('/') + "/" + endpoint.trimStart('/')
        }

        return if (query != null) {
            url.toHttpUrl().newBuilder()
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

    protected open fun buildRequestBody(builder: RequestBodyBuilder): RequestBody? {
        return builder.build(jsonAdapter)
    }

    /**
     * Build the request for OkHttp, this can be used to add the auth token for example
     *
     */
    protected open suspend fun buildRequest(builder: Request.Builder): Request {
        return builder.build()
    }

    protected open suspend fun executeRequest(request: Request): Response {
        return httpClient.newCall(request).await()
    }

    /**
     * Parse the given [response] and return the body as a [Json] object.
     *
     */
    protected open fun parseResponse(response: Response): Json {
        val responseBody = response.body

        return if (responseBody != null && responseBody.contentLength() != 0L) {
            jsonAdapter.parse(responseBody.string())
        } else {
            Json()
        }
    }

    /**
     * Verify the given [response] in order to validate it and maybe throw general exceptions.
     *
     */
    protected open fun verifyResponse(response: Response, json: Json) {
    }

    suspend fun execute(
        method: Method,
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        body: RequestBodyBuilder? = null
    ): Response {
        val url = buildUrl(endpoint, query)

        val requestBody: RequestBody? = when {
            HttpMethod.permitsRequestBody(method.name) && body != null -> buildRequestBody(body)
            HttpMethod.requiresRequestBody(method.name) -> "".toRequestBody()
            else -> null
        }

        val request = Request.Builder()
            .method(method.name, requestBody).url(url)
            .run {
                headers?.forEach {
                    addHeader(it.key, it.value)
                }
                buildRequest(this)
            }

        return executeRequest(request)
    }

    /**
     *  Execute the request and parse the response
     *
     *
     * @param method The HTTP method
     * @param endpoint It's either the path when used with a baseUrl, the full url or if the path starts with '/' will replace everything up until the base
     * @param query The request query parameters
     * @param headers The request headers
     * @param body The request body
     * @param errorHandler The primary error handler for this call, you can check and throw other errors that are not present in [verifyResponse]
     * @param responseHandler The response handler to parse the response body
     */
    open suspend fun <T> call(
        method: Method,
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        body: RequestBodyBuilder? = null,
        errorHandler: ResponseHandler<Unit>? = null,
        responseHandler: ResponseHandler<T>
    ): T {
        return withContext(Dispatchers.IO) {
            try {
                val networkResponse = execute(
                    method = method,
                    endpoint = endpoint,
                    query = query,
                    headers = headers,
                    body = body
                )

                networkResponse.body.use {
                    if (networkResponse.isSuccessful) {
                        val responseJson = try {
                            parseResponse(networkResponse)
                        } catch (e: Throwable) {
                            throw ParseException("Unable to parse request body for $endpoint", e)
                        }

                        verifyResponse(networkResponse, responseJson)
                        return@use responseHandler(responseJson)
                    } else {
                        runCatching {
                            parseResponse(networkResponse)
                        }.onSuccess {
                            errorHandler?.invoke(it)
                            verifyResponse(networkResponse, it)
                        }

                        throw HttpCodeException(
                            networkResponse.code,
                            networkResponse.message
                        )
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

suspend fun <T> ApiClient.get(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = Method.GET,
    endpoint = endpoint,
    query = query,
    headers = headers,
    errorHandler = errorHandler,
    responseHandler = response
)

suspend fun <T> ApiClient.post(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    body: RequestBodyBuilder? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = Method.POST,
    endpoint = endpoint,
    query = query,
    headers = headers,
    body = body,
    errorHandler = errorHandler,
    responseHandler = response
)

suspend fun <T> ApiClient.put(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    body: RequestBodyBuilder? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = Method.PUT,
    endpoint = endpoint,
    query = query,
    headers = headers,
    body = body,
    errorHandler = errorHandler,
    responseHandler = response
)

suspend fun <T> ApiClient.delete(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    body: RequestBodyBuilder? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = Method.DELETE,
    endpoint = endpoint,
    query = query,
    headers = headers,
    body = body,
    errorHandler = errorHandler,
    responseHandler = response
)