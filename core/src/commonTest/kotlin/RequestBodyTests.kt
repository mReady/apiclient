import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import net.mready.apiclient.builders.*
import net.mready.json.adapters.KotlinxJsonAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RequestBodyTests {
    private val jsonAdapter = KotlinxJsonAdapter()

    private fun OutgoingContent.string(): String {
        return when (this) {
            is OutgoingContent.ByteArrayContent -> bytes().decodeToString(throwOnInvalidSequence = true)
            is OutgoingContent.ContentWrapper -> delegate().string()
            is OutgoingContent.WriteChannelContent -> runBlocking {
                val channel = ByteChannel()
                writeTo(channel)
                val out = ByteArray(channel.availableForRead)
                channel.readFully(out)
                out.decodeToString(throwOnInvalidSequence = true).trimIndent()
            }

            is OutgoingContent.NoContent -> ""
            is OutgoingContent.ProtocolUpgrade -> ""
            is OutgoingContent.ReadChannelContent -> ""
        }
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
            obj["hello"] = "world"
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
            "string space" value "hello world"
            "int" value 1
            "bool" value true
        }.build(jsonAdapter)

        assertNotNull(body)
        assertEquals("string+space=hello+world&int=1&bool=true", body.string())
    }

    @Test
    fun buildEmptyUrlEncodedBody() {
        val body = formBody {}.build(jsonAdapter)

        assertNull(body)
    }

    @Test
    fun buildMultipartBody() {
        val fileInfo = FileInfo.create(
            byteArray = "content".encodeToByteArray(),
            fileName = "api-client-"
        )

        val spacesFileInfo = FileInfo.create(
            byteArray = "<html> </html>".encodeToByteArray(),
            fileName = "api client",
            contentType = ContentType.Text.Html
        )

        val body = multipartBody {
            "string" value "hello world"
            "int" value 1
            "bool" value true
            "file" file fileInfo
            "file 2" file spacesFileInfo
        }.build(jsonAdapter) as? MultiPartFormDataContent

        assertNotNull(body)

        val boundary = body.boundary

        // TODO: smarter system if more tests are written for this
        val expected = """
            --$boundary
            Content-Disposition: form-data; name=string
            Content-Length: 11

            hello world
            --$boundary
            Content-Disposition: form-data; name=int
            Content-Length: 1

            1
            --$boundary
            Content-Disposition: form-data; name=bool
            Content-Length: 4

            true
            --$boundary
            Content-Disposition: form-data; name=file; filename=${fileInfo.fileName}
            Content-Type: application/octet-stream
            Content-Length: ${fileInfo.contentLength}

            content
            --$boundary
            Content-Disposition: form-data; name="file 2"; filename="${spacesFileInfo.fileName}"
            Content-Type: text/html
            Content-Length: ${spacesFileInfo.contentLength}

            <html> </html>
            --$boundary--
           """.trimIndent()

        assertEquals(expected, body.string())
    }

    @Test
    fun buildEmptyMultipartBody() {
        val body = multipartBody {}.build(jsonAdapter) as? MultiPartFormDataContent

        assertNull(body)
    }
}