package net.mready.apiclient

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy


@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.toULong())
}