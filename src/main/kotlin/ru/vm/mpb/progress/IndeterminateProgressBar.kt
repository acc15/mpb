package ru.vm.mpb.progress

import org.fusesource.jansi.Ansi
import ru.vm.mpb.ansi.AnsiRgb

private const val LBOUND = "["
private const val CURSOR = "<=>"
private const val EMPTY = "-"
private const val RBOUND = "]"
private const val NON_EMPTY_WIDTH = LBOUND.length + CURSOR.length + RBOUND.length
private const val MIN_WIDTH = NON_EMPTY_WIDTH + EMPTY.length

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
            offset = EMPTY.length
            return this
        }

        position += offset
        if (position <= 0) {
            position = 0
            offset = EMPTY.length
        } else if (position >= maxPosition) {
            position = maxPosition
            offset = -EMPTY.length
        }
        return this
    }

    override fun apply(ansi: Ansi) {
        if (width < MIN_WIDTH) {
            return
        }
        ansi
            .a(LBOUND)
            .fgRgb(AnsiRgb.GRAY)
            .a(EMPTY.repeat(position))
            .fgBrightBlue()
            .bold()
            .a(CURSOR)
            .boldOff()
            .fgRgb(AnsiRgb.GRAY)
            .a(EMPTY.repeat(maxPosition - position))
            .reset()
            .a(RBOUND)
    }

    // [<=>---]
    // [-<=>--]
    // [--<=>-]
    // [---<=>]
    // [--<=>-]
    // [-<=>--]
    // [<=>---]

}

