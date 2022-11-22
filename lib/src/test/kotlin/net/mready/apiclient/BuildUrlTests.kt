package net.mready.apiclient

import org.junit.Test
import kotlin.test.assertEquals

class BuildUrlTests {

    @Test
    fun appendToBase() {
        val apiClient = ApiClient(baseUrl = "https://example.com/v1/")
        val url = apiClient.buildUrl(endpoint = "test")

        assertEquals("https://example.com/v1/test", url)
    }

    @Test
    fun appendToBaseWithoutFinalSlash() {
        val apiClient = ApiClient(baseUrl = "https://example.com/v1")
        val url = apiClient.buildUrl(endpoint = "test")

        assertEquals("https://example.com/v1/test", url)
    }

    @Test
    fun appendToHost() {
        val apiClient = ApiClient(baseUrl = "https://example.com/v1/")
        val url = apiClient.buildUrl(endpoint = "/v2/test")

        assertEquals("https://example.com/v2/test", url)
    }

    @Test
    fun appendToSimpleHost() {
        val apiClient = ApiClient(baseUrl = "https://example.com/")
        val url = apiClient.buildUrl(endpoint = "/v2/test")

        assertEquals("https://example.com/v2/test", url)
    }

    @Test
    fun appendToSubdomainHost() {
        val apiClient = ApiClient(baseUrl = "https://e1.example.com/v1/")
        val url = apiClient.buildUrl(endpoint = "/v2/test")

        assertEquals("https://e1.example.com/v2/test", url)
    }

    @Test
    fun appendToIp() {
        val apiClient = ApiClient(baseUrl = "https://192.168.10.0:8080/v1/")
        val url = apiClient.buildUrl(endpoint = "/v2/test")

        assertEquals("https://192.168.10.0:8080/v2/test", url)
    }

    @Test
    fun replaceBase() {
        val apiClient = ApiClient(baseUrl = "https://example.com/v1/")
        val url = apiClient.buildUrl(endpoint = "https://example2.com/v1/test")

        assertEquals("https://example2.com/v1/test", url)
    }
}