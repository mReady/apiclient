package net.mready.json

import net.mready.json.experimental.ExperimentalJsonAdapter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonWalkingTests {

    private val adapter: JsonAdapter = ExperimentalJsonAdapter

    @Test
    fun primitives() {
        val json = """
            {
                "string": "test",
                "int": 10,
                "float": 10.10,
                "long": 10,
                "double": 10.10,
                "bool": true
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertEquals("test", jsonElement["string"].string)
        assertEquals(10, jsonElement["int"].int)
        assertEquals(10.10F, jsonElement["float"].double.toFloat())
        assertEquals(10L, jsonElement["long"].long)
        assertEquals(10.10, jsonElement["double"].double)
        assertEquals(true, jsonElement["bool"].bool)
    }


    @Test
    fun jsonBody() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertEquals(10, jsonElement["test"].int)
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
        val jsonElement = adapter.parse(json)

        assertEquals(10, jsonElement["obj1"]["obj2"]["obj3"]["test"].int)
        assertFailsWith<JsonValueException> { jsonElement["obj1"]["obj2"]["obj3"]["obj4"]["test"].int }

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
        val jsonElement = adapter.parse(json)

        assertEquals(3, jsonElement.array.size)
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
        val jsonElement = adapter.parse(json)

        assertEquals(10, jsonElement.array[0].int)
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
        val jsonElement = adapter.parse(json)

        assertEquals(30, jsonElement.array.map { it.int }.sum())
    }

    @Test
    fun jsonArrayIndexOutOfBounds() {
        val json = """
            [
                10,
                10,
                10
            ]
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFailsWith<JsonValueException> { jsonElement[5].int }
    }

    @Test
    fun jsonMap() {
        val json = """
            {
                "test": 10,
                "test1": "string"
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertEquals(10, jsonElement.obj["test"]?.int)
    }

    @Test
    fun jsonMapMapper() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertEquals(10, jsonElement.obj.mapValues { it.value.int }["test"])
    }

}