package net.mready.json

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

inline fun assertFailsOn(vararg path: String, block: () -> Unit) {
    val e = assertFailsWith(JsonValueException::class, null, block)
    assertEquals(path.joinToString(" > "), e.path)
}
