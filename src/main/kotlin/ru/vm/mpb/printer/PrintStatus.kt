package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Consumer

enum class PrintStatus(val color: Ansi.Color): Consumer {
    MESSAGE(Ansi.Color.BLUE),
    WARN(Ansi.Color.YELLOW),
    SUCCESS(Ansi.Color.GREEN),
    ERROR(Ansi.Color.RED);

    override fun apply(ansi: Ansi) {
        ansi.fg(color)
    }

    fun colored(text: String) = Consumer {
        it.bold().fg(color).a(text).fgDefault().boldOff()
    }
}