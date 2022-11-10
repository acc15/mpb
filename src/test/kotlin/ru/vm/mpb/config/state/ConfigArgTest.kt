package ru.vm.mpb.config.state

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class ConfigArgTest {

    @TestFactory
    fun parse() = listOf(
        testArgs to mapOf(
            "args" to mapOf("" to "pull", "a" to listOf("a", "b")),
            "config" to "abc",
            "projects" to mapOf(
                "d" to mapOf(
                    "deps" to listOf("a", "b")
                )
            )
        ),
        arrayOf("non", "opt") to mapOf("args" to listOf("non", "opt")),
        arrayOf("--debug") to mapOf("debug" to true),
        arrayOf("--args", "a", "b") to mapOf("args" to listOf("a", "b")),
        arrayOf("--args", "a", "b", "--args.fb", "x", "y") to mapOf(
            "args" to mapOf(
                "" to listOf("a", "b"),
                "fb" to listOf("x", "y")
            )
        ),
        arrayOf("--indexed[0].value", "a", "--indexed[2].value", "b") to mapOf(
            "indexed" to listOf(
                mapOf("value" to "a"),
                null,
                mapOf("value" to "b")
            )
        ),
    ).map {
        DynamicTest.dynamicTest("parseConfigArgs: ${it.first}") {
            val expected = it.second
            val actual = ConfigArg.parse(*it.first).value
            assertEquals(expected, actual)
        }
    }


}