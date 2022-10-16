package ru.vm.mpb.config.state

import kotlin.test.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

val testArgs = listOf(
     "pull",
    "--config", "abc",
    "--projects.d.deps", "a", "b",
    "--args.a", "a", "b"
)

class ConfigStateKtTest {

    @TestFactory
    fun testParseConfigPath() = mapOf(
        "" to emptyList(),
        "a.b[0][1].c.d" to listOf("a", "b", 0, 1, "c", "d"),
        ".abc" to listOf("", "abc"),
        "[].abc" to listOf("", "abc"),
        "..." to listOf("", "", ""),
        "[ 15 ]" to listOf(15),
        "15" to listOf("15"),
        "abc[xyz]" to listOf("abc", "xyz"),
        "[abc][xyz]" to listOf("abc", "xyz"),
        "[[abc]]" to listOf("[abc]"),
        "[abc].[xyz]" to listOf("abc", "", "xyz"),
    ).map {
        DynamicTest.dynamicTest("parseConfigPath: ${it.key}") {
            val actualSegments = parseConfigPath(it.key)
            val expectedSegments = it.value
            assertEquals(expectedSegments, actualSegments)
        }
    }



    @TestFactory
    fun testParseConfigArgs() = listOf(
        testArgs to mapOf(
            "args" to mapOf("" to "pull", "a" to listOf("a", "b")),
            "config" to "abc",
            "projects" to mapOf(
                "d" to mapOf(
                    "deps" to listOf("a", "b")
                )
            )
        ),
        listOf("non", "opt") to mapOf("args" to listOf("non", "opt")),
        listOf("--debug") to mapOf("debug" to true),
        listOf("--args", "a", "b") to mapOf("args" to listOf("a", "b")),
        listOf("--args", "a", "b", "--args.fb", "x", "y") to mapOf(
            "args" to mapOf(
                "" to listOf("a", "b"),
                "fb" to listOf("x", "y")
            )
        ),
        listOf("--indexed[0].value", "a", "--indexed[2].value", "b") to mapOf(
            "indexed" to listOf(
                mapOf("value" to "a"),
                null,
                mapOf("value" to "b")
            )
        ),
    ).map {
        DynamicTest.dynamicTest("parseConfigArgs: ${it.first}") {
            val expected = it.second
            val actual = parseConfigArgs(it.first).map
            assertEquals(expected, actual)
        }
    }


}