package client

import builders.RequestBodyBuilder
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


/**
 * Make a GET request via [ApiClient.call].
 */
suspend fun <T> ApiClient.get(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = HttpMethod.Get,
    endpoint = endpoint,
    query = query,
    headers = headers,
    errorHandler = { _, json -> errorHandler?.invoke(json) },
    responseHandler = { _, json -> response(json) }
)

/**
 * Make a POST request via [ApiClient.call].
 */
suspend fun <T> ApiClient.post(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    body: RequestBodyBuilder? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = HttpMethod.Post,
    endpoint = endpoint,
    query = query,
    headers = headers,
    body = body,
    errorHandler = { _, json -> errorHandler?.invoke(json) },
    responseHandler = { _, json -> response(json) }
)

/**
 * Make a PUT request via [ApiClient.call].
 */
suspend fun <T> ApiClient.put(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    body: RequestBodyBuilder? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = HttpMethod.Put,
    endpoint = endpoint,
    query = query,
    headers = headers,
    body = body,
    errorHandler = { _, json -> errorHandler?.invoke(json) },
    responseHandler = { _, json -> response(json) }
)

/**
 * Make a DELETE request via [ApiClient.call].
 */
suspend fun <T> ApiClient.delete(
    endpoint: String,
    query: Map<String, Any?>? = null,
    headers: Map<String, String>? = null,
    body: RequestBodyBuilder? = null,
    errorHandler: ResponseHandler<Unit>? = null,
    response: ResponseHandler<T>
): T = call(
    method = HttpMethod.Delete,
    endpoint = endpoint,
    query = query,
    headers = headers,
    body = body,
    errorHandler = { _, json -> errorHandler?.invoke(json) },
    responseHandler = { _, json -> response(json) }
)

/**
 * Function to make an upload request via [ApiClient.call].
 * This combines the upload progress and response handling into a single flow.
 * This function is preferred over [ApiClient.call] for uploading as it provides a safe way to handle progress events in order.
 *
 * The flow will emit [ProgressEvent.Progress] to the flow and a final [ProgressEvent.Done] when the upload is finished
 * or will close the flow with an exception if an error occurs.
 *
 * The Done event will contain the result of the response handling or error handling if an error occurred.
 */
fun <T> ApiClient.upload(
    method: HttpMethod,
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
            errorHandler = { _, json -> errorHandler?.invoke(json) },
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
            responseHandler = { _, json -> response(json) }
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