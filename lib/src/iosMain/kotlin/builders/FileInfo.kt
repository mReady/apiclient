package builders

import io.ktor.http.*
import platform.Foundation.NSData
import toByteArray

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

    actual companion object {
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