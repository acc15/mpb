package ru.vm.mpb.progressbar

import kotlin.math.abs

data class Interpolator(val start: Int, val end: Int) {

    val total get() = abs(end - start)
    val incrementing get() = start <= end
    val step get() = if (incrementing) 1 else -1
    val progression get() = IntProgression.fromClosedRange(start, end, step)

    fun overlap(value: Int): Int = if (incrementing) when {
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

    fun normalize(value: Int) = abs(clamp(value) - start)

    fun interpolate(value: Int, toStart: Int, toEnd: Int): Int {
        if (start == end) {
            return toStart
        }
        when (overlap(value)) {
            -1 -> return toStart
            1 -> return toEnd
        }

        val t = total
        val v = abs(value - start)
        return (toStart * (t - v) + toEnd * v) / t
    }

    fun interpolate(value: Int, to: Interpolator) = interpolate(value, to.start, to.end)

    override fun toString() = "$start..$end"
}

fun interpolateRgbComponent(value: Int, from: Interpolator, to: Interpolator, mask: Int): Int {
    val shift = mask.countTrailingZeroBits()
    val start = to.start and mask shr shift
    val end = to.end and mask shr shift
    return from.interpolate(value, start, end) shl shift
}

fun interpolateRgb(value: Int, from: Interpolator, to: Interpolator): Int =
    interpolateRgbComponent(value, from, to, 0xff) or
    interpolateRgbComponent(value, from, to, 0xff00) or
    interpolateRgbComponent(value, from, to, 0xff0000)
