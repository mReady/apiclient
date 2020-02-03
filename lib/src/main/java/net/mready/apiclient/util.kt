package net.mready.apiclient

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


//currently OkHttp doesn't have an await for Call, maybe in the future will have and this will not be needed anymore
suspend fun Call.await() = suspendCancellableCoroutine<Response> { c ->
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            c.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            if (!c.isCancelled) {
                c.resumeWithException(e)
            }
        }
    })

    c.invokeOnCancellation {
        if (c.isCancelled)
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
    }
}