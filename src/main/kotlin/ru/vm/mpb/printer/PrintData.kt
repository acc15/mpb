package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi

data class PrintData(
    val msg: Any?,
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
            .bold().a('[').fgBrightGreen().a(key).fgDefault().a(']').boldOff()
            .a(' ').a(msg).reset().toString()
    }
}