package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi

data class PrintData(
    val msg: Any?,
    val status: PrintStatus,
    val key: String
) {
    fun format(monochrome: Boolean): String {
        if (key.isEmpty()) {
            return msg.toString()
        }
        if (monochrome) {
            return "[$key] $msg"
        }
        return Ansi.ansi()
            .bold().a('[').also { status.ansiColor(it) }.a(key).fgDefault().a(']').reset()
            .a(' ').a(msg).toString()
    }
}