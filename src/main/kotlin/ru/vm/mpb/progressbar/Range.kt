package ru.vm.mpb.progressbar

import kotlin.math.abs

data class Range(val start: Int, val end: Int) {

    val total get() = distance + 1
    val distance get() = abs(end - start)
    val progression get() = IntProgression.fromClosedRange(start, end, if (start <= end) 1 else -1)

    fun overlap(value: Int): Int = if (start <= end) when {
        value < start -> -1
        value > end -> 1
        else -> 0
    } else when {
        value > start -> -1
        value < end -> 1
        else -> 0
    }

    fun clamp(value: Int) = when (overlap(value)) {
        -1 -> start
        1 -> end
        else -> value
    }

    fun zeroBased(value: Int) = abs(value - start)
    fun normalize(value: Int) = zeroBased(clamp(value))

    fun interpolate(value: Int, toStart: Int, toEnd: Int): Int {
        if (start == end) {
            return toStart
        }
        when (overlap(value)) {
            -1 -> return toStart
            1 -> return toEnd
        }

        val t = distance
        val v = zeroBased(value)
        return (toStart * (t - v) + toEnd * v) / t
    }

    fun interpolate(value: Int, to: Range) = interpolate(value, to.start, to.end)

    fun interpolateRgb(value: Int, toRgb: Range) =
        interpolateRgbComponent(value, toRgb, 0xff) or
        interpolateRgbComponent(value, toRgb, 0xff00) or
        interpolateRgbComponent(value, toRgb, 0xff0000)

    private fun interpolateRgbComponent(value: Int, toRgb: Range, mask: Int): Int {
        val shift = mask.countTrailingZeroBits()
        val start = toRgb.start and mask shr shift
        val end = toRgb.end and mask shr shift
        return interpolate(value, start, end) shl shift
    }

    override fun toString() = "$start..$end"

    companion object {
        fun fromTotal(total: Int) = Range(0, total - 1)
    }
}
