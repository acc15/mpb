package ru.vm.mpb.config

import kotlin.test.*

class BranchPatternTest {

    @Test
    fun findBranch() {

        val branches = listOf(
            "origin/abc",
            "origin/rc/3.415.1-sf",
            "origin/rc/3.415.3-sf"
        )

        val pattern = BranchPattern(Regex("^(\\d+)$"), "^origin/(rc/3\\.$1\\..*-sf)$", -1)

        assertEquals("rc/3.415.3-sf", pattern.findMatch("415", branches))
        assertNull(pattern.findMatch("x415", branches))

    }

    @Test
    fun getBranchRegex() {
        val p = BranchPattern(
            Regex("(\\d+)\\.([\\d|\\[\\]]+)"),
            "^origin/(rc/3\\.$1\\.(?:$2)-sf)$",
            -1
        )

        val input = "415.3|4,416.[35]"
        val actual = p.getRegexes(input).map { it.pattern }
        val expected = listOf(
            "^origin/(rc/3\\.415\\.(?:3|4)-sf)$",
            "^origin/(rc/3\\.416\\.(?:[35])-sf)$"
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun getBranchMatch() {
        @Suppress("RegExpSingleCharAlternation", "RegExpUnnecessaryNonCapturingGroup") val patterns = listOf(
            Regex("^origin/(rc/3\\.415\\.(?:3|4)-sf)$"),
            Regex("^origin/(rc/3\\.416\\.(?:[35])-sf)$")
        )

        assertEquals(null, BranchPattern.getBranchMatch(patterns, "origin/rc/3.415.2-sf"))
        assertEquals("rc/3.415.3-sf", BranchPattern.getBranchMatch(patterns, "origin/rc/3.415.3-sf"))
        assertEquals("rc/3.415.4-sf", BranchPattern.getBranchMatch(patterns, "origin/rc/3.415.4-sf"))
        assertEquals("rc/3.416.3-sf", BranchPattern.getBranchMatch(patterns, "origin/rc/3.416.3-sf"))
        assertEquals(null, BranchPattern.getBranchMatch(patterns, "origin/rc/3.416.4-sf"))
        assertEquals("rc/3.416.5-sf", BranchPattern.getBranchMatch(patterns, "origin/rc/3.416.5-sf"))
        assertEquals(null, BranchPattern.getBranchMatch(patterns, "origin/rc/3.415.6-sf"))
    }

    @Test
    fun getBranchesMatch() {
        val patterns = listOf(
            BranchPattern(Regex("^(\\d+)$"), "^origin/(rc/3\\.$1\\..*-sf)$", -1),
            BranchPattern(Regex("^(\\d+)\\.([\\d?:()|\\[\\]\\\\]+)$"), "^origin/(rc/3\\.$1\\.(?:$2)-sf)$", -1)
        )

        val list1 = listOf(
            "origin/rc/3.415.2-sf",
            "origin/rc/3.415.4-sf",
            "origin/rc/3.415.6-sf"
        )
        
        val list2 = listOf(
            "origin/rc/3.415.1-sf",
            "origin/rc/3.415.3-sf",
            "origin/rc/3.415.5-sf"
        )

        assertEquals("rc/3.415.6-sf", BranchPattern.findMatch(patterns, list1, "415"))
        assertEquals("rc/3.415.5-sf", BranchPattern.findMatch(patterns, list2, "415"))

        assertEquals("rc/3.415.4-sf", BranchPattern.findMatch(patterns, list1, "415.3|4"))
        assertEquals("rc/3.415.3-sf", BranchPattern.findMatch(patterns, list2, "415.3|4"))

    }
}