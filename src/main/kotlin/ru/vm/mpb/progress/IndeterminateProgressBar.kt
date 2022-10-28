package ru.vm.mpb.progress

import kotlinx.coroutines.*
import org.fusesource.jansi.Ansi.Consumer
import ru.vm.mpb.util.redirectStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

class IndeterminateProgressBar(private var position: Int = 0) {

    private var offset: Int = 0
    private val EMPTY_CONSUMER = Consumer {}

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
                .fgBrightDefault()
                .a(LBOUND)
                .fgRgb(0x7f, 0x7f, 0x7f)
                .a(EMPTY.repeat(position))
                .fgBrightBlue()
                .bold()
                .a(CURSOR)
                .boldOff()
                .fgRgb(0x7f, 0x7f, 0x7f)
                .a(EMPTY.repeat(maxPosition - position))
                .fgBrightDefault()
                .a(RBOUND)
                .reset()
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

suspend fun streamProgress(
    vararg streams: Pair<InputStream, OutputStream>,
    callback: (IndeterminateProgressBar) -> Unit
) = withContext(Dispatchers.Default) {
    val changeFlag = AtomicBoolean(false)
    val progressJob = launch {
        val progress = IndeterminateProgressBar()
        while (isActive) {
            if (changeFlag.getAndSet(false)) {
                callback(progress)
            }
            delay(50)
        }
    }
    withContext(Dispatchers.IO) {
        for (s in streams) {
            redirectStream(s.first, s.second) { changeFlag.set(true) }
        }
    }
    progressJob.cancelAndJoin()
}