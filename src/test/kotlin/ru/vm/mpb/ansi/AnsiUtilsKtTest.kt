package ru.vm.mpb.ansi

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class AnsiUtilsKtTest {

    @TestFactory
    fun stripAnsi() = mapOf<Ansi, String>(
        ansi().bold().boldOff() to "",
        ansi().cursorDownLine().eraseScreen().a(123) to "123",
        ansi().fgBrightDefault().a("333").fgDefault() to "333"
    ).map {
        DynamicTest.dynamicTest("stripAnsi: ${it.key}") {
            assertEquals(it.value, stripAnsi(it.key.toString()))
        }
    }

    @Test
    fun noAnsi() {
        val v = ru.vm.mpb.ansi.noAnsi()
        assertEquals("abc", v.bold().a("abc").boldOff().toString())
    }
}