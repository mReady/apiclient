import io.ktor.utils.io.core.*
import net.mready.apiclient.toByteArray
import net.mready.apiclient.toNSData
import kotlin.test.Test
import kotlin.test.assertEquals


class NSDataTest {

    @Test
    fun testNsDataConversions() {
        val helloString = "Hello, World!"
        val byteArray = helloString.toByteArray()

        assertEquals(helloString, byteArray.decodeToString())
        assertEquals(helloString, byteArray.toNSData().toByteArray().decodeToString())
    }
}