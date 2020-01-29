package net.mready.json

import net.mready.json.experimental.ExperimentalJsonAdapter
import net.mready.json.experimental.JsonElement
import org.junit.Test
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

//@RunWith(Parameterized::class)
class JsonBuilderTests {
    private val adapter: JsonAdapter = ExperimentalJsonAdapter

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Array<Array<Any?>>? {
            return Array(1000) { arrayOfNulls<Any>(0) }
        }
    }

    @Test
    fun buildSimpleObject() {
        val json = jsonObject(adapter) {
            "hello" value "world"
        }

        assertEquals("world", json["hello"].string)
        assertEquals("""{"hello":"world"}""", json.toJsonString(prettyPrint = false))
    }

    @Test
    fun objectWithPrimitiveValues() {
        val json = jsonObject(adapter) {
            "string" value "hello"
            "int" value 1
            "long" value 1L
            "double" value 1.0
            "bool" value true
            "null" value null
        }

        assertEquals("hello", json["string"].string)
        assertEquals(1, json["int"].int)
        assertEquals(1L, json["long"].long)
        assertEquals(1.0, json["double"].double)
        assertEquals(true, json["bool"].bool)
        assertEquals(true, json["null"].isNull)

        assertEquals(
            """{"string":"hello","int":1,"long":1,"double":1.0,"bool":true,"null":null}""",
            json.toJsonString(prettyPrint = false)
        )
    }

    @Test
    fun objectWithNestedObject() {
        val json = jsonObject(adapter) {
            "obj" jsonObject {
                "hello" value "world"
            }
        }

        assertEquals("world", json["obj"]["hello"].string)

        assertEquals("""{"obj":{"hello":"world"}}""", json.toJsonString(prettyPrint = false))
    }

    @Test
    fun objectWithNestedArray() {
        val json = jsonObject(adapter) {
            "arr" jsonArray {
                array += 1
            }
        }

        assertEquals(1, json["arr"][0].int)

        assertEquals("""{"arr":[1]}""", json.toJsonString(prettyPrint = false))
    }

    @Test
    fun buildSimpleArray() {
        val json = jsonArray(adapter) {
            array += 1
            array += 2
            array += 3
        }

        assertEquals(listOf(1, 2, 3), json.array.map { it.int })
        assertEquals("""[1,2,3]""", json.toJsonString(prettyPrint = false))
    }

    @Test
    fun arrayWithPrimitiveValues() {
        val json = jsonArray(adapter) {
            array += "hello"
            array += 1
            array += 1L
            array += 1.0
            array += true
            array += null
        }

        assertEquals("hello", json[0].string)
        assertEquals(1, json[1].int)
        assertEquals(1L, json[2].long)
        assertEquals(1.0, json[3].double)
        assertEquals(true, json[4].bool)
        assertEquals(true, json[5].isNull)

        assertEquals("""["hello",1,1,1.0,true,null]""", json.toJsonString(prettyPrint = false))
    }

    @Test
    fun arrayWithNestedObject() {
        val json = jsonArray(adapter) {
            array += jsonObject {
                "hello" value "world"
            }
        }

        assertEquals("world", json[0]["hello"].string)

        assertEquals("""[{"hello":"world"}]""", json.toJsonString(prettyPrint = false))
    }

    @Test
    fun arrayWithNestedArray() {
        val json = jsonArray(adapter) {
            array += jsonArray {
                array += 1
            }
        }

        assertEquals(1, json[0][0].int)

        assertEquals("""[[1]]""", json.toJsonString(prettyPrint = false))
    }

    @Test
    fun test() {
        val json = JsonElement()
        json["null"] = null
        json["hello"] = 123
        json["obj"]["sub"] = "1234"
        json["arr"][1] = true
        json["arr2"] += 1
        json["arr2"] += 2

        assertEquals(true, json["null"].isNull)
        assertEquals(123, json["hello"].int)
        assertEquals("1234", json["obj"]["sub"].string)
        assertEquals(true, json["arr"][0].isNull)
        assertEquals(true, json["arr"][1].bool)
        assertEquals(1, json["arr2"][0].int)
        assertEquals(2, json["arr2"][1].int)

        println(json.toJsonString(prettyPrint = true))

        assertEquals(
            """{"null":null,"hello":123,"obj":{"sub":"1234"},"arr":[null,true],"arr2":[1,2]}""",
            json.toJsonString(prettyPrint = false)
        )
    }
}