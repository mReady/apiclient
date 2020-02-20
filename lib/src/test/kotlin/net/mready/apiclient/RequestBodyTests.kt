package net.mready.apiclient

import net.mready.json.Json
import net.mready.json.getDefaultAdapter
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RequestBodyTests {
    private val jsonAdapter = Json.getDefaultAdapter()

    private fun RequestBody.string(): String {
        val buffer = Buffer()
        writeTo(buffer)
        return buffer.readUtf8()
    }

    @Test
    fun buildRawBody() {
        val body = rawBody("hello").build(jsonAdapter)

        assertNotNull(body)
        assertEquals("hello", body.string())
    }

    @Test
    fun buildEmptyRawBody() {
        val body = rawBody(null).build(jsonAdapter)

        assertNull(body)
    }

    @Test
    fun buildJsonObjectBody() {
        val body = jsonObjectBody {
            "hello" value "world"
        }.build(jsonAdapter)

        assertNotNull(body)
        assertEquals("""{"hello":"world"}""", body.string())
    }

    @Test
    fun buildEmptyJsonObjectBody() {
        val body = jsonObjectBody {}.build(jsonAdapter)

        assertNotNull(body)
        assertEquals("""{}""", body.string())
    }

    @Test
    fun buildJsonArrayBody() {
        val body = jsonArrayBody {
            array += 1
        }.build(jsonAdapter)

        assertNotNull(body)
        assertEquals("""[1]""", body.string())
    }

    @Test
    fun buildEmptyJsonArrayBody() {
        val body = jsonArrayBody {}.build(jsonAdapter)

        assertNotNull(body)
        assertEquals("""[]""", body.string())
    }

    @Test
    fun buildUrlEncodedBody() {
        val body = formBody {
            "string" value "hello world"
            "int" value 1
            "bool" value true
        }.build(jsonAdapter)

        assertNotNull(body)
        assertEquals("string=hello%20world&int=1&bool=true", body.string())
    }

    @Test
    fun buildEmptyUrlEncodedBody() {
        val body = formBody {}.build(jsonAdapter)

        assertNull(body)
    }

    @Test
    fun buildMultipartBody() {
        val file = File.createTempFile("api-client-", null)
        file.deleteOnExit()

        file.writeText("content")

        val body = multipartBody {
            "string" value "hello world"
            "int" value 1
            "bool" value true
            "file" file file
        }.build(jsonAdapter) as? MultipartBody

        assertNotNull(body)

        val boundary = body.boundary

        // TODO: smarter system if more tests are written for this
        val expected = """
            --$boundary
            Content-Disposition: form-data; name="string"
            Content-Length: 11

            hello world
            --$boundary
            Content-Disposition: form-data; name="int"
            Content-Length: 1

            1
            --$boundary
            Content-Disposition: form-data; name="bool"
            Content-Length: 4

            true
            --$boundary
            Content-Disposition: form-data; name="file"; filename="${file.name}"
            Content-Type: application/octet-stream
            Content-Length: 7

            content
            --$boundary--
            
        """.trimIndent()

        assertEquals(expected, body.string().replace("\r", ""))
    }

    @Test
    fun buildEmptyMultipartBody() {
        val body = multipartBody {}.build(jsonAdapter) as? MultipartBody

        assertNull(body)
    }
}