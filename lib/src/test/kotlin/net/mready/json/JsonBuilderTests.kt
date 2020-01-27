package net.mready.json

import net.mready.json.impl.KotlinxJsonValue
import org.junit.Test
import kotlin.test.assertEquals

class JsonBuilderTests {
    private val adapter: JsonAdapter = KotlinxJsonValue

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
}