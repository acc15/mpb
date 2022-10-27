package ru.vm.mpb.util

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole

fun <T> withJansi(disabled: Boolean, callback: () -> T): T {
    Ansi.setDetector { !disabled }
    try {
        AnsiConsole.systemInstall()
        return callback()
    } finally {
        AnsiConsole.systemUninstall()
    }
}
