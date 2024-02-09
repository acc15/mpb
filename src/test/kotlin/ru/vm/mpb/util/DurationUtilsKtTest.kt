package ru.vm.mpb.util

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import ru.vm.mpb.ansi.ansi
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DurationUtilsKtTest {
    @TestFactory
    fun prettyString() = mapOf(
        5.seconds to "5s",
        5.seconds + 947.milliseconds to "5s 947ms",
        65.seconds to "1m 5s",
        (90 * 60).seconds to "1h 30m",
        (60 * 60 * 28 + 30 * 60).seconds to "1d 4h 30m"
    ).map {
        DynamicTest.dynamicTest("pretty: ${it.key}") {
            assertEquals(it.value, ansi(false).get().apply(it.key.pretty).toString())
        }
    }
}