package ru.vm.mpb.progressbar

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Consumer

private const val ELLIPSIS = "..."
private const val MIN_WIDTH = 1

class ColoredProgressBar(
    var width: Int,
    var amount: Int = 0,
    var total: Int = 0,
    var text: String = ""
): ProgressBar {

    private var fillText = ""
    private var emptyText = ""

    override fun update(): ProgressBar {
        if (width < MIN_WIDTH) {
            fillText = ""
            emptyText = ""
            return this
        }

        val fillWidth = if (total > 0) amount * width / total else 0
        val innerText = spaceCentered(ellipsized(text, width), width)
        fillText = innerText.substring(0, fillWidth)
        emptyText = innerText.substring(fillWidth, width)
        return this
    }

    private fun ellipsized(s: String, w: Int): String {
        val l = s.length
        val el = ELLIPSIS.length
        return when {
            w >= l -> s
            el >= l -> ""
            else -> s.substring(0, w - el) + ELLIPSIS
        }
    }

    private fun spaceCentered(s: String, w: Int): String {
        val l = s.length
        if (l >= w) {
            return s
        }

        val et = w - l
        val het = et / 2
        return " ".repeat(het + et % 2) + s + " ".repeat(het)
    }

    override fun apply(ansi: Ansi) {
        if (width < MIN_WIDTH) {
            return
        }
        ansi
            .bg(Ansi.Color.GREEN).fg(Ansi.Color.BLACK)
            .a(fillText)
            .bg(Ansi.Color.BLUE).fg(Ansi.Color.BLACK)
            .a(emptyText)
            .reset()
    }
}