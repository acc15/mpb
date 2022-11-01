package ru.vm.mpb.ansi

import org.fusesource.jansi.Ansi
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class AnsiStripKtTest {

    @TestFactory
    fun stripAnsi() = mapOf<Ansi, String>(
        ansi(true).get().bold().boldOff() to "",
        ansi(true).get().cursorDownLine().eraseScreen().a(123) to "123",
        ansi(true).get().fgBrightDefault().a("333").fgDefault() to "333"
    ).map {
        DynamicTest.dynamicTest("stripAnsi: ${it.key}") {
            assertEquals(it.value, stripAnsi(it.key.toString()))
        }
    }
}