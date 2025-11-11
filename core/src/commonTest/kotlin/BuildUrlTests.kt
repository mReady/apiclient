import io.ktor.client.*
import io.ktor.client.engine.cio.*
import net.mready.apiclient.client.ApiClient
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildUrlTests {

    @Test
    fun appendToBase() {
        val apiClient = ApiClient(
            baseUrl = "https://example.com/v1/",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "test").buildString()

        assertEquals("https://example.com/v1/test", url)
    }

    @Test
    fun appendToBaseWithoutFinalSlash() {
        val apiClient = ApiClient(
            baseUrl = "https://example.com/v1",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "test").buildString()

        assertEquals("https://example.com/v1/test", url)
    }

    @Test
    fun appendToHost() {
        val apiClient = ApiClient(
            baseUrl = "https://example.com/v1/",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "/v2/test").buildString()

        assertEquals("https://example.com/v2/test", url)
    }

    @Test
    fun appendToSimpleHost() {
        val apiClient = ApiClient(
            baseUrl = "https://example.com/",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "/v2/test").buildString()

        assertEquals("https://example.com/v2/test", url)
    }

    @Test
    fun appendToSubdomainHost() {
        val apiClient = ApiClient(
            baseUrl = "https://e1.example.com/v1/",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "/v2/test").buildString()

        assertEquals("https://e1.example.com/v2/test", url)
    }

    @Test
    fun appendToIp() {
        val apiClient = ApiClient(
            baseUrl = "https://192.168.10.0:8080/v1/",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "/v2/test").buildString()

        assertEquals("https://192.168.10.0:8080/v2/test", url)
    }

    @Test
    fun replaceBase() {
        val apiClient = ApiClient(
            baseUrl = "https://example.com/v1/",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "https://example2.com/v1/test").buildString()

        assertEquals("https://example2.com/v1/test", url)
    }

    @Test
    fun replacePathSegmentsFromBaseUrl() {
        val apiClient = ApiClient(
            baseUrl = "https://example.com/v1/path1/path2/",
            httpClient = HttpClient(CIO)
        )
        val url = apiClient.buildUrl(endpoint = "/test/1/2/3").buildString()

        assertEquals("https://example.com/test/1/2/3", url)
    }
}