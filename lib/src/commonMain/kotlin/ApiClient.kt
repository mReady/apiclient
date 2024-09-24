@file:Suppress("unused")

package net.mready.apiclient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.mready.json.Json
import net.mready.json.JsonAdapter
import net.mready.json.adapters.KotlinxJsonAdapter

class HttpCodeException(val code: Int, message: String) : RuntimeException(message)
class ParseException(message: String, cause: Throwable?) : RuntimeException(message, cause)

sealed class ProgressEvent<out T> {
    class Progress<T>(val bytesSentTotal: Long, val contentLength: Long?) : ProgressEvent<T>()
    class Done<out T>(val data: T) : ProgressEvent<T>()
}

class ApiException(message: String) : RuntimeException(message)

typealias ProgressHandler = suspend (bytesSentTotal: Long, contentLength: Long?) -> Unit
typealias ResponseHandler<T> = (Json) -> T

enum class Method {
    GET, POST, PUT, DELETE
}

open class ApiClient(
    private val baseUrl: String = "",
    protected val httpClient: HttpClient = HttpClient(CIO),
    protected val jsonAdapter: JsonAdapter = KotlinxJsonAdapter()
) {

    fun buildUrl(endpoint: String, query: Map<String, Any?>? = null): URLBuilder {

        val url = if (endpoint.startsWith("http")) {
            endpoint
        } else if (endpoint.startsWith("/")) {
            URLBuilder(baseUrl).apply {
                pathSegments = endpoint.trimStart('/').split('/')
            }.toString()
        } else {
            baseUrl.trimEnd('/') + "/" + endpoint.trimStart('/')
        }

        val urlBuilder = URLBuilder(url)

        query?.forEach { (key, value) ->
            if (value != null) {
                urlBuilder.parameters.apply {
                    append(key, value.toString())
                }
            }
        }

        return urlBuilder
    }

    protected open fun buildRequestBody(builder: RequestBodyBuilder): OutgoingContent? = builder.build(jsonAdapter)

    /**
     * Build the request, this can be used to add the auth token for example.
     *
     */
    protected open suspend fun buildRequest(builder: HttpRequestBuilder): HttpRequestBuilder = builder

    protected open suspend fun executeRequest(requestBuilder: HttpRequestBuilder): HttpResponse =
        httpClient.request(requestBuilder)


    /**
     * Parse the given [response] and return the body as a [Json] object.
     *
     */
    protected open suspend fun parseResponse(response: HttpResponse): Json = jsonAdapter.parse(response.bodyAsText())

    /**
     * Verify the given [response] in order to validate it and maybe throw general exceptions.
     *
     */
    protected open fun verifyResponse(response: HttpResponse, json: Json) {
    }

    suspend fun execute(
        method: Method,
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        uploadProgress: ProgressHandler? = null,
        body: RequestBodyBuilder? = null
    ): HttpResponse {
        val urlBuilder = buildUrl(endpoint, query)

        val requestBody: OutgoingContent? = when {
            body != null -> buildRequestBody(body)
            else -> EmptyContent
        }

        val request = HttpRequestBuilder().apply {
            this.url.takeFrom(urlBuilder)
            this.method = HttpMethod.parse(method.name.uppercase())
            this.setBody(requestBody)
            headers?.forEach {
                this.headers.append(it.key, it.value)
            }

            if (uploadProgress != null) {
                onUpload { bytesSentTotal, contentLength ->
                    val length = if (contentLength == -1L) {
                        null
                    } else {
                        contentLength
                    }
                    uploadProgress(bytesSentTotal, length)
                }
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
        uploadProgress: ProgressHandler? = null,
        responseHandler: ResponseHandler<T>
    ): T = withContext(Dispatchers.IO) {
        try {
            val networkResponse = execute(
                method = method,
                endpoint = endpoint,
                query = query,
                headers = headers,
                uploadProgress = uploadProgress,
                body = body
            )

            if (networkResponse.status.isSuccess()) {
                val responseJson = try {
                    parseResponse(networkResponse)
                } catch (e: Throwable) {
                    throw ParseException(
                        message = "Unable to parse response body for $endpoint",
                        cause = e
                    )
                }

                verifyResponse(networkResponse, responseJson)
                return@withContext responseHandler(responseJson)
            } else {
                runCatching {
                    parseResponse(networkResponse)
                }.onSuccess {
                    errorHandler?.invoke(it)
                    verifyResponse(networkResponse, it)
                }

                throw HttpCodeException(
                    networkResponse.status.value,
                    networkResponse.status.description
                )
            }

        } catch (e: Throwable) {
            throw e
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

fun <T> ApiClient.upload(
    method: Method,
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    body: RequestBodyBuilder? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): Flow<ProgressEvent<T>> = channelFlow {
    val mutex = Mutex()
    var sendProgress = true

    try {
        call(
            method = method,
            endpoint = endpoint,
            query = query,
            headers = headers,
            body = body,
            errorHandler = errorHandler,
            uploadProgress = { sent, total ->
                mutex.withLock {
                    if (sendProgress) {
                        send(
                            ProgressEvent.Progress(
                                bytesSentTotal = sent,
                                contentLength = total
                            )
                        )
                    }
                }
            },
            responseHandler = response
        ).also { result ->
            mutex.withLock {
                send(ProgressEvent.Done(result))
                sendProgress = false
            }
        }
    } catch (t: Throwable) {
        close(t)
    }
}