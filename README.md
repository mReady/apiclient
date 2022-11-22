ApiClient
=========

This library is a wrapper on top of [OkHttp][okhttp] and [FluidJson][fluidjson] to facilitate both manual and automatic deserialization when working with APIs that are not well formatted.
Works on Android 5.0+ (API level 21+) and Java 8+.

Take for example the following JSON response:

```json
{
  "data": {
    "user": {
      "info": {
        "name": "John",
        "email": "john@example.com"
      }
    }
  }
}
```

Manual deserialization
---

```kotlin
val apiClient = ApiClient(baseUrl = "https://example.com/")

launch {
    val user = apiClient.get(
        endpoint = "user"
    ) { json ->
        User(
            name = json["data"]["user"]["info"]["name"].string,
            email = json["data"]["user"]["info"]["email"].string
        )
    }
    
    user.name
}
```


```kotlin
val apiClient = ApiClient(baseUrl = "https://example.com/")

launch {
    val user = apiClient.put(
        endpoint = "user",
        body = jsonObjectBody {
            obj["name"] = "Doe"
        }
    ) { json ->
        User(
            name = json["data"]["name"].string, //in this case the response has another format
            email = json["data"]["email"].string
        )
    }
}
```

Automatic deserialization
---

Don't forget to apply the [Kotlin Serialization][serialization] plugin

```kotlin
@Serializable
data class ResponseWrapper<T>(
    val data: T
)

@Serializable
data class UserWrapper(
    val user: UserInfo
)

@Serializable
data class UserInfo(
    val info: User
)

@Serializable
data class User(
    val name: String,
    val email: String
)

val apiClient = ApiClient(baseUrl = "https://example.com/")

launch {
    val userResponse = apiClient.get(
        endpoint = "user"
    ) { json ->
        json.decode<ResponseWrapper<UserWrapper>>()
    }

    userResponse.data.user.info.name
}
```

Setup
--------

The latest release is available on [Maven Central](https://search.maven.org/artifact/net.mready.apiclient/apiclient).

```kotlin
implementation("net.mready.apiclient:apiclient:1.0.0")
```

License
-------

```
Copyright 2022 mReady Solutions SRL.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[okhttp]: https://github.com/square/okhttp
[fluidjson]: https://github.com/mReady/FluidJson
[serialization]: https://github.com/Kotlin/kotlinx.serialization