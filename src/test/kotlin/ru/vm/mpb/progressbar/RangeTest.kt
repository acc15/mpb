package ru.vm.mpb.progressbar

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

class RangeTest {

    private fun i(start: Int, end: Int) = Range(start, end)

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

    @Test
    fun interpolateRgb() {
        val src = i(0, 15)
        val dst = i(0xff0000, 0xff)
        val expected = listOf(
            0xff0000, 0xee0011, 0xdd0022, 0xcc0033, 0xbb0044, 0xaa0055, 0x990066, 0x880077,
            0x770088, 0x660099, 0x5500aa, 0x4400bb, 0x3300cc, 0x2200dd, 0x1100ee, 0x0000ff
        )
        val actual = src.progression.map { src.interpolateRgb(it, dst) }
        assertContentEquals(expected, actual)
    }

}