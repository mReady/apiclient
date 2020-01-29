package net.mready.json

import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class InvalidInputTests {
    private val adapter: JsonAdapter = DefaultJsonAdapter

    @Test
    fun emptyDeserialization() {
        val json = ""
        val jsonElement = adapter.parse(json)

        assertFailsWith<JsonValueException> { jsonElement.int }
    }

    @Test
    fun stringDeserialization() {
        val json = """
            "test"
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFailsWith<JsonValueException> { jsonElement.int }
    }

    @Test
    fun jsonInvalid() {
        val json = """
            "test": 75
        """.trimIndent()

        assertFailsWith<JsonParseException> { adapter.parse(json) }
    }

    @Test
    fun jsonBodyMissingValue() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFailsWith<JsonValueException> { jsonElement["test1"].int }
    }

    @Test
    fun jsonArrayNull() {
        val json = """
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFailsWith<JsonValueException> { jsonElement.array.map { it.int } }
    }

    @Test
    fun jsonMap() {
        val json = """
            [
                {
                    "test": 10
                }
            ]
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFailsWith<JsonValueException> { jsonElement.obj }
    }

    @Test
    fun jsonInvalidTypeString() {
        val json = """
            {
                "test": 10,
                "test1": 10
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFailsWith<JsonValueException> { jsonElement["test"].string }
        assertNull(jsonElement["test1"].stringOrNull)
    }

    @Test
    fun jsonBodyMissingValueNull() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertNull(jsonElement["test1"].intOrNull)
    }

    @Test
    fun jsonRequireObjectWithArrayInput() {
        val json = """
            [
                {
                    "test": 10
                }
            ]
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertNull(jsonElement.objOrNull)
    }

}