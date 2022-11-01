package ru.vm.mpb.ansi

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.Consumer
import org.fusesource.jansi.AnsiConsole
import java.lang.StringBuilder
import java.util.function.Supplier

fun <T> withAnsi(callback: () -> T): T {
    try {
        AnsiConsole.systemInstall()
        return callback()
    } finally {
        AnsiConsole.systemUninstall()
    }
}

fun Ansi.applyIf(condition: Boolean, callback: Consumer): Ansi {
    if (condition) {
        apply(callback)
    }
    return this
}

