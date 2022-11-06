package ru.vm.mpb.progressbar

import org.fusesource.jansi.Ansi
import ru.vm.mpb.ansi.AnsiRgb
import java.lang.StrictMath.abs

private const val MIN_WIDTH = 1

class IndeterminateProgressBar(
    var width: Int,
    var position: Int = 0,
    var offset: Int = 0
): ProgressBar {

    private val colorRange = Range(AnsiRgb.GREEN, AnsiRgb.BLUE)
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

    override fun apply(ansi: Ansi) {
        if (width < MIN_WIDTH) {
            return
        }

        ansi.fgBlack()

        val p = Range(0, width * 3 / 4)
        var lastRgb = 0
        for (i in 0 until width) {
            val rgb = p.interpolateRgb(abs(i - position), colorRange)
            if (i == 0 || rgb != lastRgb) {
                ansi.bgRgb(rgb)
                lastRgb = rgb
            }
            ansi.a(' ')
        }
        ansi.reset()
    }

}

