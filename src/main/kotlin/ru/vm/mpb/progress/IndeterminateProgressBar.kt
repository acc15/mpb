package ru.vm.mpb.progress

import kotlinx.coroutines.*
import org.fusesource.jansi.Ansi.Consumer
import ru.vm.mpb.util.transferTo
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

private val EMPTY_CONSUMER = Consumer {}

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

    fun update(width: Int): Consumer {
        if (width < MIN_WIDTH) {
            position = 0
            offset = 0
            return EMPTY_CONSUMER
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

        return Consumer { it
                .a(LBOUND)
                .fgRgb(0x7f, 0x7f, 0x7f)
                .a(EMPTY.repeat(position))
                .fgBrightBlue()
                .bold()
                .a(CURSOR)
                .boldOff()
                .fgRgb(0x7f, 0x7f, 0x7f)
                .a(EMPTY.repeat(maxPosition - position))
                .reset()
                .a(RBOUND)
        }
    }


    // [<=>---]
    // [-<=>--]
    // [--<=>-]
    // [---<=>]
    // [--<=>-]
    // [-<=>--]
    // [<=>---]

}

