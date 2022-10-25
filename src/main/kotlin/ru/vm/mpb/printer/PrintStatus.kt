package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi

enum class PrintStatus(val ansiColor: (Ansi) -> Ansi) {
    MESSAGE({ it.fgBrightBlue() }),
    WARN({ it.fgBrightYellow() }),
    SUCCESS({ it.fgBrightGreen() }),
    ERROR({ it.fgBrightRed() })
}