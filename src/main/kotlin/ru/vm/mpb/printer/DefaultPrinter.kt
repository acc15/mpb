package ru.vm.mpb.printer

import java.io.PrintStream

class DefaultPrinter(val out: PrintStream): Printer {
    override fun print(data: PrintData) {
        out.println(data.msg)
    }
}