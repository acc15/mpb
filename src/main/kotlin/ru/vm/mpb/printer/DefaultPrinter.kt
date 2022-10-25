package ru.vm.mpb.printer

import java.io.PrintStream

class DefaultPrinter(val out: PrintStream, val formatter: PrintFormatter): Printer {
    override fun print(data: PrintData) {
        out.println(formatter(data))
    }
}