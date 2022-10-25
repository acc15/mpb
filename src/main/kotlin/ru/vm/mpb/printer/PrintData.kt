package ru.vm.mpb.printer

import org.fusesource.jansi.Ansi
typealias PrintFormatter = (PrintData) -> String

data class PrintData(
    val msg: Any?,
    val key: String
) {
    fun format(colors: Boolean = false): String {
        if (key.isEmpty()) {
            return msg.toString()
        }
        if (!colors) {
            return "[$key] $msg"
        }
        return Ansi.ansi()
            .bold().a('[').fgBrightGreen().a(key).fgDefault().a(']').boldOff()
            .a(' ').a(msg).reset().toString()
    }
}