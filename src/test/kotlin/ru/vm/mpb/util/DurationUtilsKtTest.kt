package ru.vm.mpb.util

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import ru.vm.mpb.ansi.ansi
import java.time.Duration
import kotlin.test.assertEquals

class DurationUtilsKtTest {
    @TestFactory
    fun prettyString() = mapOf(
        Duration.ofSeconds(5) to "5s",
        Duration.ofSeconds(5, 947_000_000) to "5s 947ms",
        Duration.ofSeconds(65) to "1m 5s",
        Duration.ofSeconds(90 * 60) to "1h 30m",
        Duration.ofSeconds(60 * 60 * 28 + 30 * 60) to "1d 4h 30m"
    ).map {
        DynamicTest.dynamicTest("pretty: ${it.key}") {
            assertEquals(it.value, ansi(false).get().apply(it.key.pretty).toString())
        }
    }
}