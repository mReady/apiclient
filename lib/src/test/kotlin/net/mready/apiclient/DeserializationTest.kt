package net.mready.apiclient

import com.beust.klaxon.Klaxon
import net.mready.apiclient.deserializers.KlaxonJsonSerializer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull


class DeserializationTest {

    private val jsonSerializer = KlaxonJsonSerializer(Klaxon())

    @Test
    fun emptyDeserialization() {
        val json = """
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertFails { jsonElement.int() }
    }

    @Test
    fun jsonBody() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(10, jsonElement["test"].int())
    }

    @Test
    fun jsonBodyMissingValueNull() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertNull(jsonElement["test1"].intOrNull())
    }

    @Test
    fun jsonBodyMissingValueElse() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(5, jsonElement["test1"].intOrElse { 5 })
    }

    @Test
    fun jsonBodyMissingValueThrow() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertFails { jsonElement["test1"].int() }
    }

    @Test
    fun jsonPath() {
        val json = """
            {
                "obj1": {
                    "obj2": {
                        "obj3": {
                            "test": 10
                        }
                    }
                }
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(10, jsonElement["obj1"]["obj2"]["obj3"]["test"].int())
    }

    @Test
    fun jsonPathThrow() {
        val json = """
            {
                "obj1": {
                    "obj2": {
                        "obj3": {
                            "test": 10
                        }
                    }
                }
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

       assertFails { jsonElement["obj1"]["obj2"]["obj3"]["obj4"]["test"].int() }
    }

    @Test
    fun jsonArray() {
        val json = """
            [
                10,
                10,
                10
            ]
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(3, jsonElement.asList().size)
    }

    @Test
    fun jsonArrayJsonElement() {
        val json = """
            [
                10,
                10,
                10
            ]
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(10, jsonElement.asList()[0].int())
    }

    @Test
    fun jsonArrayMapInt() {
        val json = """
            [
                10,
                10,
                10
            ]
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(30, jsonElement.asList { it.int() }.sum())
    }

    @Test
    fun jsonArrayNullThrow() {
        val json = """
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertFails { jsonElement.asList { it.int() } }
    }

    @Test
    fun jsonArrayNull() {
        val json = """
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(null, jsonElement.asListOrNull { it.int() })
    }

    @Test
    fun jsonMap() {
        val json = """
            {
                "test": 10,
                "test1": "string"
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(10, jsonElement.asMap()["test"]?.int())
    }

    @Test
    fun jsonMapMapper() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertEquals(10, jsonElement.asMap { it.int() }["test"])
    }

    @Test
    fun jsonMapThrow() {
        val json = """
            [
                {
                    "test": 10
                }
            ]
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertFails { jsonElement.asMap() }
    }

    @Test
    fun jsonMapNull() {
        val json = """
            [
                {
                    "test": 10
                }
            ]
        """.trimIndent()
        val jsonElement = jsonSerializer.parse(json)

        assertNull(jsonElement.asMapOrNull())
    }
}