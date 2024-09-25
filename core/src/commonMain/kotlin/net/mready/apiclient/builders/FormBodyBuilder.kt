package net.mready.apiclient.builders

import io.ktor.client.request.forms.*
import io.ktor.http.*
import net.mready.json.Json
import net.mready.json.JsonAdapter

@ApiDsl
/**
 * Builder for form data content.
 *
 * @sample formBodySample
 */
class FormBodyBuilder : RequestBodyBuilder {
    val values = mutableListOf<Pair<String, Any?>>() //Any: String | Number | Boolean | Json

    /**
     * Assign a string [value] to a string key in the form data.
     */
    infix fun String.value(value: String?) {
        values.add(this to value)
    }

    /**
     * Assign a number [value] to a string key in the form data.
     */
    infix fun String.value(value: Number?) {
        values.add(this to value)
    }

    /**
     * Assign a boolean [value] to a string key in the form data.
     */
    infix fun String.value(value: Boolean?) {
        values.add(this to value)
    }

    /**
     * Assign a [Json] [value] to a string key in the form data.
     */
    infix fun String.value(value: Json?) {
        values.add(this to value)
        formData {

        }
    }

    /**
     * @see RequestBodyBuilder.build
     */
    override fun build(adapter: JsonAdapter): FormDataContent? {
        if (values.isEmpty()) return null

        return FormDataContent(formData = parameters {
            values.forEach { (key, value) ->
                when (value) {
                    is Json -> append(key, adapter.stringify(value))
                    is String -> append(key, value)
                    else -> append(key, value.toString())
                }
            }
        })
    }
}

/**
 * Build form body with [FormBodyBuilder] via [block].
 */
fun formBody(block: FormBodyBuilder.() -> Unit): RequestBodyBuilder {
    return FormBodyBuilder().apply(block)
}
