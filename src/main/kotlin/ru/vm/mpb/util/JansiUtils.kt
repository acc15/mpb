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
