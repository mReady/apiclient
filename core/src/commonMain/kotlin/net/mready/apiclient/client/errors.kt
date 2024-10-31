package net.mready.apiclient.client

/**
 * Exception thrown when the server returns an error code.
 */
class HttpCodeException(val code: Int, message: String) : RuntimeException(message)

/**
 * Exception thrown when the response body cannot be parsed.
 */
class ParseException(message: String, cause: Throwable?) : RuntimeException(message, cause)