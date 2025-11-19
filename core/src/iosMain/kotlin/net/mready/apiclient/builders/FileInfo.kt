package net.mready.apiclient.builders

import io.ktor.http.*
import net.mready.apiclient.toByteArray
import platform.Foundation.NSData
import kotlin.experimental.ExperimentalObjCName

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FileInfo(
    actual val byteArray: ByteArray,
    actual val fileName: String,
    actual val contentType: ContentType = FileInfo.defaultContentType
) {
    actual val contentLength: Long = byteArray.size.toLong()

    constructor(data: NSData, fileName: String, contentType: ContentType = FileInfo.defaultContentType) : this(
        byteArray = data.toByteArray(),
        fileName = fileName,
        contentType = contentType
    )

    @OptIn(ExperimentalObjCName::class)
    actual companion object {
        @ObjCName("defaultContentType")
        fun defaultContentType(): ContentType = FileInfo.defaultContentType
        internal actual fun create(
            byteArray: ByteArray,
            fileName: String,
            contentType: ContentType
        ): FileInfo = FileInfo(
            byteArray = byteArray,
            fileName = fileName,
            contentType = contentType
        )
    }
}