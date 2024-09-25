package client

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlin.coroutines.CoroutineContext


/**
 * Wrapper class for [HttpResponse] that does not expose the response body in order to prevent accidental double reading
 * when response is parsed by [client.ApiClient.call] method.
 *
 * If you need a custom handle for [HttpResponse],use [client.ApiClient.execute] directly.
 */
class NetworkResponseInfo private constructor(
    val request: HttpRequest,
    val headers: Headers,
    val version: HttpProtocolVersion,
    val requestTime: GMTDate,
    val responseTime: GMTDate,
    val coroutineContext: CoroutineContext,
    val status: HttpStatusCode,
) {
    companion object {
        fun fromHttpResponse(response: HttpResponse): NetworkResponseInfo {
            return NetworkResponseInfo(
                request = response.request,
                headers = response.headers,
                version = response.version,
                requestTime = response.requestTime,
                responseTime = response.responseTime,
                coroutineContext = response.coroutineContext,
                status = response.status
            )
        }
    }
}