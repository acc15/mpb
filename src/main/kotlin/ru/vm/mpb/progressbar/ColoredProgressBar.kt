package ru.vm.mpb.progressbar

import org.fusesource.jansi.Ansi
import ru.vm.mpb.ansi.AnsiRgb

private const val ELLIPSIS = "..."
private const val MIN_WIDTH = 1

class ColoredProgressBar(
    var width: Int,
    var current: Int = 0,
    var total: Int = 0,
    var text: String = ""
): ProgressBar {

    private val pixelColor = Range(AnsiRgb.BLUE, AnsiRgb.GREEN)
    private val pixelRange = Range.fromTotal(10)

    override fun update() {
        current = minOf(current, total)
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

        val innerText = spaceCentered(ellipsized(text, width), width)
        val pixelSteps = pixelRange.total
        val alignedWidth = width * pixelSteps
        val current = if (total > 0) current * alignedWidth / total else 0

        a.fgBlack()

        val fill = current / pixelSteps
        if (fill > 0) {
            a.bgRgb(AnsiRgb.GREEN).a(innerText.substring(0, fill))
        }
        if (fill < width) {
            a.bgRgb(pixelRange.interpolateRgb(current % pixelSteps, pixelColor)).a(innerText[fill])
        }
        if (fill + 1 < width) {
            a.bgRgb(AnsiRgb.BLUE).a(innerText.substring(fill + 1))
        }

        a.reset()
    }
}