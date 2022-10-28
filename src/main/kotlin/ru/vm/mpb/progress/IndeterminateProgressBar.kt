package ru.vm.mpb.progress

import org.fusesource.jansi.Ansi

class IndeterminateProgressBar(private var position: Int = 0) {

    private var offset: Int = 0

    companion object {

        private const val LBOUND = "["
        private const val CURSOR = "<=>"
        private const val EMPTY = "-"
        private const val RBOUND = "]"
        private const val NON_EMPTY_WIDTH = LBOUND.length + CURSOR.length + RBOUND.length
        private const val MIN_WIDTH = NON_EMPTY_WIDTH + EMPTY.length

        fun maxPosition(width: Int) = width - MIN_WIDTH

    }

    fun update(width: Int, ansi: Ansi) {
        if (width < MIN_WIDTH) {
            position = 0
            offset = 0
            return
        }

        val maxPosition = maxPosition(width)
        position += offset
        if (position <= 0) {
            position = 0
            offset = EMPTY.length
        } else if (position >= maxPosition) {
            position = maxPosition
            offset = -EMPTY.length
        } else if (offset == 0) {
            offset = EMPTY.length
        }

        ansi
            .fgBright(Ansi.Color.WHITE)
            .a(LBOUND)
            .fgRgb(0x7f, 0x7f, 0x7f)
            .a(EMPTY.repeat(position))
            .fgBrightBlue()
            .bold()
            .a(CURSOR)
            .boldOff()
            .fgRgb(0x7f, 0x7f, 0x7f)
            .a(EMPTY.repeat(maxPosition - position))
            .fgBright(Ansi.Color.WHITE)
            .a(RBOUND)
            .reset()
    }


    // [<=>---]
    // [-<=>--]
    // [--<=>-]
    // [---<=>]
    // [--<=>-]
    // [-<=>--]
    // [<=>---]

}