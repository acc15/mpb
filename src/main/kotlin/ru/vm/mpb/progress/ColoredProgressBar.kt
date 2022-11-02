package ru.vm.mpb.progress

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Consumer

private const val LBOUND = "["
private const val RBOUND = "]"
private const val BOUND_WIDTH = LBOUND.length + RBOUND.length
private const val MIN_WIDTH = BOUND_WIDTH + 1

data class ColoredProgressBar(
    var width: Int,
    var amount: Int,
    var total: Int,
    var message: String = "",
    var fill: Consumer = Consumer { it.bgGreen().fgBlack() },
    var empty: Consumer = Consumer { it.bgDefault().fgDefault() },
    var bound: Consumer = Consumer { it.reset() }
): ProgressBar {

    private var innerWidth = 0
    private var fillWidth = 0

    override fun update(): ProgressBar {
        if (width < MIN_WIDTH) {
            innerWidth = 0
            fillWidth = 0
            return this
        }
        TODO("implement")
    }

    override fun apply(ansi: Ansi) {
        if (innerWidth <= 0) {
            return
        }
        ansi
            .apply(bound)
            .a(LBOUND)
            .apply(fill)
            .a(" ".repeat(fillWidth))
            .apply(empty)
            .a(" ".repeat(innerWidth - fillWidth))
            .apply(bound)
            .a(RBOUND)
            .reset()
    }
}