package ru.vm.mpb.progressbar

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class InterpolatorTest {

    private fun i(start: Int, end: Int) = Interpolator(start, end)

    @Test
    fun progression() {
        assertContentEquals(listOf(-2, -1, 0, 1), i(-2, 1).progression.toList())
        assertContentEquals(listOf(1, 0, -1, -2), i(1, -2).progression.toList())
    }

    @Test
    fun clamp() {
        val v1 = i(1, -1)
        val actual = listOf(-10, -2, -1, 0, 1, 2, 10).map { v1.clamp(it) }
        val expected = listOf(-1, -1, -1, 0, 1, 1, 1)
        assertContentEquals(expected, actual)
    }

    @Test
    fun normalize() {
        val v1 = i(1, -1)
        val actual = listOf(-10, -2, -1, 0, 1, 2, 10).map { v1.normalize(it) }
        val expected = listOf(2, 2, 2, 1, 0, 0, 0)
        assertContentEquals(expected, actual)
    }

    @TestFactory
    fun interpolate() = listOf(
        (i(1, -1) to i(-100, -80)) to listOf(-100, -90, -80),
        (i(-1, 1) to i(-100, -80)) to listOf(-100, -90, -80),
        (i(5, 2) to i(10, -20)) to listOf(10, 0, -10, -20),
        (i(1, 1) to i(-100, -80)) to listOf(-100)
    ).map { (i, expected) ->
        DynamicTest.dynamicTest("interpolate ${i.first} to ${i.second}: $expected") {
            val actual = i.first.progression.map { i.first.interpolate(it, i.second) }
            assertContentEquals(expected, actual)
        }
    }


}