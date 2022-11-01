package ru.vm.mpb.ansi

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class AnsiFactoryKtTest {

    @Test
    fun noAnsi() {
        val v = ansi(false).get()
        Assertions.assertEquals("abc", v.bold().a("abc").boldOff().toString())
    }

    @TestFactory
    fun ansiParent() = listOf(
        false to true,
        false to false,
        true to true,
        true to false
    ).map {
        DynamicTest.dynamicTest("ansiParent: ${it.first} -> ${it.second}") {
            val parent = ansi(it.first).get().a("1")
            val child = ansi(it.second).get(parent).a("2")
            parent.a("3")
            assertEquals("13", parent.toString())
            assertEquals("12", child.toString())
        }
    }

    @Test
    fun ansiToNoAnsiMustStripRegex() {
        val parent = ansi(true).get().fgBlack().a("1").fgDefault()
        val child = ansi(false).get(parent).a("2")
        assertEquals("12", child.toString())
    }

}