package net.mready.apiclient.builders

import net.mready.apiclient.client.ApiClient
import net.mready.apiclient.client.post
import net.mready.json.jsonObject

val client = ApiClient()

/**
 * Sample for [RawBodyBuilder]
 */

internal suspend fun rawBodySample() {
    client.post(
        endpoint = "https://api.example.com/post",
        //Output hello world
        body = rawBody("hello world"),
        response = {}
    )
}

/**
 * Sample for [JsonArrayBodyBuilder]
 */
internal suspend fun jsonArrayBodySample() {
    val lst = listOf("hello", "world")

    client.post(
        endpoint = "https://api.example.com/post",
        //Output [["hello","world"],{"key":"value"}]
        body = jsonArrayBody {
            array += lst
            array += jsonObject {
                obj["key"] = "value"
            }
        },
        response = {}
    )
}

/**
 * Sample for [JsonObjectBodyBuilder]
 */
internal suspend fun jsonObjectBodySample() {
    client.post(
        endpoint = "https://api.example.com/post",
        //Output {"hello":"world","key":"value"}
        body = jsonObjectBody {
            obj["hello"] = "world"
            obj["key"] = "value"
        },
        response = {}
    )
}

/**
 * Sample for [FormBodyBuilder]
 */
internal suspend fun formBodySample() {
    client.post(
        endpoint = "https://api.example.com/post",
        //Output string=value&int=1&bool=true&json+object=%7B%22key%22%3A%22value%22%7D
        body = formBody {
            "string" value "value"
            "int" value 1
            "bool" value true
            "json object" value jsonObject {
                obj["key"] = "value"
            }
        },
        response = {}
    )
}

/**
 * Sample for [MultiPartBodyBuilder]
 */

internal suspend fun multipartBodySample() {
    client.post(
        endpoint = "https://api.example.com/post",
        //Output --2d9f80da6931971f-5866bf107a07089e518bbd29-2d0c99be215afc997dcab1c661c
        //Content-Disposition: form-data; name=string
        //Content-Length: 11
        //
        //hello world
        //---2d9f80da6931971f-5866bf107a07089e518bbd29-2d0c99be215afc997dcab1c661c
        //Content-Disposition: form-data; name=int
        //Content-Length: 1
        //
        //1
        //---2d9f80da6931971f-5866bf107a07089e518bbd29-2d0c99be215afc997dcab1c661c
        //Content-Disposition: form-data; name=bool
        //Content-Length: 4
        //
        //true
        body = multipartBody {
            "string" value "hello world"
            "int" value 1
            "bool" value true
        },
        response = {}
    )
}
