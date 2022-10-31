package ru.vm.mpb.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BranchPatternTest {

    @Test
    fun findBranch() {

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