package ru.vm.mpb.regex

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class RegexUtilsKtTest {

    @TestFactory
    fun escapeReplacement() = mapOf(
        "abc$$" to "abc\\$\\$",
        "abc$1" to "abc$1",
        "abc\${name}" to "abc\${name}",
        "abc\\xyz" to "abc\\\\xyz",
        "\\abc\\xyz" to "\\\\abc\\\\xyz"
    ).map {
        DynamicTest.dynamicTest("escapeReplacement: ${it.key}") {
            val actual = escapeReplacement(it.key)
            val expected = it.value
            assertEquals(expected, actual)
        }
    }

    @TestFactory
    fun replaceByLookups() = listOf<Pair<Pair<String, Map<*, String>>, String>>(
        ("\${project}@\${id}" to mapOf("project" to "test", "id" to "common")) to "test@common",
        ("abc\$1" to mapOf(1 to "xyz")) to "abcxyz",
        ("replace by \${spec} and value" to mapOf("spec" to "name")) to "replace by name and value",
        ("abc\$2" to mapOf(1 to "xyz")) to "abc", // missing group
        ("abc\${missing}" to mapOf(1 to "xyz")) to "abc", // missing named group
        ("abc\\\${escaped}value" to mapOf("escaped" to "xyz")) to "abc\${escaped}value",
        ("abc\\\$1" to mapOf(1 to "xyz")) to "abc\$1",
    ).map {
        DynamicTest.dynamicTest("replaceByLookups: ${it.first.first} (${it.first.second}") {
            val repl = it.first.second
            val actual = replaceByLookup(it.first.first,
                { index -> repl[index].orEmpty() },
                { name -> repl[name].orEmpty() }
            )
            val expected = it.second
            assertEquals(expected, actual)
        }
    }


}