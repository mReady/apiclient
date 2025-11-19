package net.mready.apiclient.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import net.mready.apiclient.builders.RequestBodyBuilder
import net.mready.json.Json
import net.mready.json.JsonAdapter
import net.mready.json.adapters.KotlinxJsonAdapter

/**
 * A sealed class representing the progress of a request.
 *
 * @property T The type of the data that will be returned when the request is done.
 */
sealed class ProgressEvent<out T> {
    class Progress<T>(val bytesSentTotal: Long, val contentLength: Long?) : ProgressEvent<T>()
    class Done<out T>(val data: T) : ProgressEvent<T>()

    //iOS helpers
    fun isProgress(): Boolean = this is Progress<T>
    fun isDone(): Boolean = this is Done<T>
    fun <T> progress() = this as? Progress<*>
    fun <T> done() = this as? Done<*>
}


typealias ProgressHandler = suspend (bytesSentTotal: Long, contentLength: Long?) -> Unit
typealias ErrorHandler = (NetworkResponseInfo, Json) -> Unit
typealias RichResponseHandler<T> = (NetworkResponseInfo, Json) -> T
typealias ResponseHandler<T> = (Json) -> T

/**
 * Class responsible for making API calls.
 *
 * @property baseUrl The base url for the API.
 * @property httpClient The HTTP client to use for requests.
 * @property jsonAdapter The JSON adapter to use for parsing and serializing JSON.
 */
open class ApiClient(
    private val baseUrl: String,
    protected val httpClient: HttpClient,
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
     */
    protected open suspend fun buildRequest(builder: HttpRequestBuilder): HttpRequestBuilder = builder

    protected open suspend fun executeRequest(requestBuilder: HttpRequestBuilder): HttpResponse =
        httpClient.request(requestBuilder)


    /**
     * Parse the given [response] and return the body as a [Json] object.
     */
    protected open suspend fun parseResponse(response: HttpResponse): Json =
        jsonAdapter.parse(response.bodyAsText())

    /**
     * Verify the given [response] in order to validate it and maybe throw general exceptions.
     */
    protected open fun verifyResponse(response: HttpResponse, json: Json) {
    }

    /**
     * Execute the request and return the response.
     *
     * @param method The HTTP method
     * @param endpoint It's either the path when used with a baseUrl, the full url or if the path starts with '/' will replace everything up until the base
     * @param query The request query parameters
     * @param headers The request headers
     * @param uploadProgress The progress handler for the upload
     * @param body The request body build via [RequestBodyBuilder]
     */
    suspend fun execute(
        method: HttpMethod,
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
            this.method = method
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
     *  If the response could not be parsed it will throw a [ParseException]
     *
     *  For server errors, you can override [verifyResponse] and throw the error there to have a global handle
     *  or use the [errorHandler] to handle the error only for the current call and throw it.
     *  If you don't throw an error in either [verifyResponse] or [errorHandler] [HttpCodeException] will be thrown.
     *
     *  [errorHandler] takes precedence over [verifyResponse].
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
        method: HttpMethod,
        endpoint: String,
        query: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        body: RequestBodyBuilder? = null,
        errorHandler: ErrorHandler? = null,
        uploadProgress: ProgressHandler? = null,
        responseHandler: RichResponseHandler<T>
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
            val networkResponseInfo = NetworkResponseInfo.fromHttpResponse(networkResponse)

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
                return@withContext responseHandler(networkResponseInfo, responseJson)
            } else {
                runCatching {
                    parseResponse(networkResponse)
                }.onSuccess {
                    errorHandler?.invoke(networkResponseInfo, it)
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