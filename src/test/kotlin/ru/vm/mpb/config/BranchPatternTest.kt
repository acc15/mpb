package ru.vm.mpb.config

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BranchPatternTest {

    @TestFactory
    internal fun escapeReplacement() = mapOf(
        "abc$$" to "abc\\$\\$",
        "abc$1" to "abc$1",
        "abc\${name}" to "abc\${name}",
        "abc\\xyz" to "abc\\\\xyz",
        "\\abc\\xyz" to "\\\\abc\\\\xyz"
    ).map {
        DynamicTest.dynamicTest("escapeReplacement: ${it.key}") {
            val actual = BranchPattern.escapeReplacement(it.key)
            val expected = it.value
            assertEquals(expected, actual)
        }
    }

    @Test
    internal fun findBranch() {

        val branches = listOf(
            "origin/abc",
            "origin/rc/3.415.1-sf",
            "origin/rc/3.415.3-sf"
        )

        val pattern = BranchPattern(Regex("^(\\d+)$"), "^origin/(rc/3\\.$1\\..*-sf)$", -1)

        assertEquals("rc/3.415.3-sf", pattern.findBranch("415", branches))
        assertNull(pattern.findBranch("x415", branches))

    }

}