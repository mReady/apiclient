package net.mready.apiclient.builders

import io.ktor.http.content.*
import net.mready.apiclient.client.ApiClient
import net.mready.json.JsonAdapter

/**
 * Class used to build request body for api calls with [ApiClient].
 */
interface RequestBodyBuilder {
    /**
     * Builds the request body. This function is called by the [ApiClient.execute] to build the request body.
     *
     * @param adapter The json adapter to use for serializing the body. By default, it uses the JsonAdapter provided by the [ApiClient].
     * @return The built request body or null if there is nothing to build.
     */
    fun build(adapter: JsonAdapter): OutgoingContent?
}

@DslMarker
annotation class ApiDsl