package ru.vm.mpb.progress

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Consumer

private const val LBOUND = "|"
private const val RBOUND = "|"
private const val ELLIPSIS = "..."
private const val BOUND_WIDTH = LBOUND.length + RBOUND.length
private const val MIN_WIDTH = BOUND_WIDTH + 1

class ColoredProgressBar(
    var width: Int,
    var amount: Int = 0,
    var total: Int = 0,
    var message: String = "",
    var fill: Consumer = Consumer { it.bgGreen().fgBlack() },
    var empty: Consumer = Consumer { it.bgDefault().fgDefault() },
    var bound: Consumer = Consumer { it.reset() }
): ProgressBar {

    private var innerWidth = 0
    private var fillWidth = 0
    private var fillText = ""
    private var emptyText = ""

    override fun update(): ProgressBar {
        if (width < MIN_WIDTH) {
            innerWidth = 0
            fillWidth = 0
            return this
        }

        innerWidth = width - BOUND_WIDTH
        fillWidth = if (total > 0) amount * innerWidth / total else 0

        val innerText = spaceCentered(ellipsized(message, innerWidth), innerWidth)
        fillText = innerText.substring(0, fillWidth)
        emptyText = innerText.substring(fillWidth, innerWidth)
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
        if (innerWidth <= 0) {
            return
        }
        ansi
            .apply(bound)
            .a(LBOUND)
            .apply(fill)
            .a(fillText)
            .apply(empty)
            .a(emptyText)
            .apply(bound)
            .a(RBOUND)
            .reset()
    }
}