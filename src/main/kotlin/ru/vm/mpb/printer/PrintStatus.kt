package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi.Consumer

enum class PrintStatus(val consumer: Consumer): Consumer by consumer {
    MESSAGE({ it.fgBrightBlue() }),
    WARN({ it.fgBrightYellow() }),
    SUCCESS({ it.fgBrightGreen() }),
    ERROR({ it.fgBrightRed() });
}