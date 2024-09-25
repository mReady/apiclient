package builders

import io.ktor.http.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class FileInfo(
    actual val byteArray: ByteArray,
    actual val fileName: String,
    actual val contentType: ContentType = FileInfo.defaultContentType
) {
    actual val contentLength: Long = byteArray.size.toLong()

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