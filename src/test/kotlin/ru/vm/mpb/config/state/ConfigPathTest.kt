package ru.vm.mpb.config.state

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class ConfigPathTest {

    @TestFactory
    fun parse() = mapOf(
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
        DynamicTest.dynamicTest("parse: ${it.key}") {
            val actualSegments = ConfigPath.parse(it.key)
            val expectedSegments = it.value
            assertEquals(expectedSegments, actualSegments)
        }
    }

    @Test
    fun parseTransformsKebabCase() {
        assertEquals(listOf("branch", "noFetch"), ConfigPath.parse("branch.no-fetch"))
        assertEquals(listOf("branch", "noFetch"), ConfigPath.parse("branch.noFetch"))
    }

}