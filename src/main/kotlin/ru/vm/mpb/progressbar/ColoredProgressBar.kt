package ru.vm.mpb.progressbar

import org.fusesource.jansi.Ansi
import ru.vm.mpb.ansi.AnsiRgb

private const val ELLIPSIS = "..."
private const val MIN_WIDTH = 1

class ColoredProgressBar(
    var width: Int,
    var amount: Int = 0,
    var total: Int = 0,
    var text: String = ""
): ProgressBar {

    private var innerText = ""

//    private var fillText = ""
//    private var emptyText = ""

    override fun update(): ProgressBar {
        if (width < MIN_WIDTH) {
            innerText = ""
            return this
        }
        innerText = spaceCentered(ellipsized(text, width), width)
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

    override fun apply(a: Ansi) {
        if (width < MIN_WIDTH) {
            return
        }

        val fill = if (total > 0) amount * width / total else 0
        a.fgBlack().bgRgb(AnsiRgb.GREEN).a(innerText.substring(0, fill))
        if (fill < width) {
            val remainder = if (total > 0) (amount * width) % total else 0
            a.bgRgb(AnsiRgb.BLUE).a(innerText[fill])
        }
        if (fill + 1 < width) {
            a.bgRgb(AnsiRgb.BLUE).a(innerText.substring(fill + 1))
        }
        a.reset()
    }
}