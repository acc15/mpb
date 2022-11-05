package ru.vm.mpb.progressbar

import org.fusesource.jansi.Ansi
import java.lang.StrictMath.abs

private const val MIN_WIDTH = 1

class IndeterminateProgressBar(
    var width: Int,
    var position: Int = 0,
    var offset: Int = 0
): ProgressBar {

    private var maxPosition: Int = 0

    override fun update(): ProgressBar {
        if (width < MIN_WIDTH) {
            position = 0
            offset = 0
            maxPosition = 0
            return this
        }

        maxPosition = width - MIN_WIDTH
        if (offset == 0) {
            offset = 1
            return this
        }

        position += offset
        if (position <= 0) {
            position = 0
            offset = 1
        } else if (position >= maxPosition) {
            position = maxPosition
            offset = -1
        }
        return this
    }

    private val colorRange = Interpolator(0xff00, 0xff)

    override fun apply(ansi: Ansi) {
        if (width < MIN_WIDTH) {
            return
        }

        val p = Interpolator(0, width - 1)

        var prevRgb = 0
        for (i in 0 until width) {
            val rgb = interpolateRgb(abs(i - position), p, colorRange)
            if (i == 0 || rgb != prevRgb) {
                ansi.bgRgb(rgb)
                prevRgb = rgb
            }
            ansi.a(' ')
        }
        ansi.reset()
    }

    // [<=>---]
    // [-<=>--]
    // [--<=>-]
    // [---<=>]
    // [--<=>-]
    // [-<=>--]
    // [<=>---]

}

