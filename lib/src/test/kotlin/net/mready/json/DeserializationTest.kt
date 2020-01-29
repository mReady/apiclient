package net.mready.json

import net.mready.json.experimental.ExperimentalJsonAdapter
import org.junit.Test
import org.junit.runners.Parameterized
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

//@RunWith(Parameterized::class)
class DeserializationTest {

    private val adapter: JsonAdapter = ExperimentalJsonAdapter

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Array<Array<Any?>>? {
            return Array(100) { arrayOfNulls<Any>(0) }
        }
    }

    @Test
    fun emptyDeserialization() {
        val json = """
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFails { jsonElement.int }
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
    fun jsonBodyMissingValueThrow() {
        val json = """
            {
                "test": 10
            }
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFails { jsonElement["test1"].int }
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
        val jsonElement = adapter.parse(json)

        assertFails { jsonElement["obj1"]["obj2"]["obj3"]["obj4"]["test"].int }
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
    fun jsonArrayNullThrow() {
        val json = """
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFails { jsonElement.array.map { it.int } }
    }

    @Test
    fun jsonArrayNull() {
        val json = """
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertEquals(null, jsonElement.arrayOrNull?.map { it.int })
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

    @Test
    fun jsonMapThrow() {
        val json = """
            [
                {
                    "test": 10
                }
            ]
        """.trimIndent()
        val jsonElement = adapter.parse(json)

        assertFails { jsonElement.obj }
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
        val jsonElement = adapter.parse(json)

        assertNull(jsonElement.objOrNull)
    }
}