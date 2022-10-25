package ru.vm.mpb.util

import org.fusesource.jansi.AnsiConsole

fun <T> withJansi(callback: () -> T): T {
    try {
        AnsiConsole.systemInstall()
        return callback()
    } finally {
        AnsiConsole.systemUninstall()
    }
}

fun terminalWidth(): Int {
    val w = AnsiConsole.getTerminalWidth()
    return if (w <= 0) 80 else w
}
